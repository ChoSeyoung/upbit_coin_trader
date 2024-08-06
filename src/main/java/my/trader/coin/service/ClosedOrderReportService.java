package my.trader.coin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import my.trader.coin.enums.UpbitType;
import my.trader.coin.model.ClosedOrder;
import my.trader.coin.model.ClosedOrderReport;
import my.trader.coin.repository.ClosedOrderReportRepository;
import my.trader.coin.repository.ClosedOrderRepository;
import org.springframework.stereotype.Service;

@Service
public class ClosedOrderReportService {
  private final ClosedOrderRepository closedOrderRepository;
  private final ClosedOrderReportRepository closedOrderReportRepository;

  public ClosedOrderReportService(ClosedOrderRepository closedOrderRepository,
                                  ClosedOrderReportRepository closedOrderReportRepository) {
    this.closedOrderRepository = closedOrderRepository;
    this.closedOrderReportRepository = closedOrderReportRepository;
  }

  public void generateReport() {
    // 리포트 테이블을 truncate 합니다.
    closedOrderReportRepository.truncateTable();

    // 거래한 종목을 가져옵니다.
    List<String> markets = closedOrderRepository.findDistinctMarkets();

    // 각 종목별로 매수와 매도 거래를 조회하여 수익률을 계산하고 저장합니다.
    for (String market : markets) {
      List<ClosedOrder> orders = closedOrderRepository.findByMarketOrderByCreatedAtAsc(market);

      BigDecimal totalBuyCost = BigDecimal.ZERO;
      BigDecimal totalSellRevenue = BigDecimal.ZERO;
      double totalBuyVolume = 0.0;
      double totalSellVolume = 0.0;

      for (ClosedOrder order : orders) {
        if (UpbitType.ORDER_SIDE_BID.getType().equalsIgnoreCase(order.getSide())) {
          totalBuyCost = totalBuyCost.add(order.getExecutedFunds());
          totalBuyVolume += order.getVolume();
        } else if (UpbitType.ORDER_SIDE_ASK.getType().equalsIgnoreCase(order.getSide())) {
          totalSellRevenue = totalSellRevenue.add(order.getExecutedFunds());
          totalSellVolume += order.getVolume();
        }
      }

      BigDecimal averageBuyPrice = totalBuyVolume == 0 ? BigDecimal.ZERO :
            totalBuyCost.divide(BigDecimal.valueOf(totalBuyVolume), RoundingMode.HALF_UP);

      BigDecimal realizedProfit = totalSellRevenue.subtract(
            averageBuyPrice.multiply(BigDecimal.valueOf(totalSellVolume)));

      System.out.println(realizedProfit);
      saveClosedOrderReport(market, realizedProfit);
    }
  }

  public void saveClosedOrderReport(String market, BigDecimal realizedProfit) {
    ClosedOrderReport report = new ClosedOrderReport();
    report.setMarket(market);
    report.setAmount(realizedProfit);
    closedOrderReportRepository.save(report);
  }
}
