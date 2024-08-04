package my.trader.coin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
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

  public void generateHourlyReport() {
    // 리포트 테이블을 truncate 합니다.
    closedOrderReportRepository.truncateTable();

    // 1. distinct market 목록을 가져옵니다.
    List<String> markets = closedOrderRepository.findDistinctMarkets();

    // 2. 각 market별로 매수와 매도 거래를 조회하여 수익률을 계산하고 저장합니다.
    for (String market : markets) {
      List<ClosedOrder> orders = closedOrderRepository.findByMarket(market);

      List<ClosedOrder> buys = new ArrayList<>();
      List<ClosedOrder> sells = new ArrayList<>();

      for (ClosedOrder order : orders) {
        if (order.getSide().equals(UpbitType.ORDER_SIDE_BID.getType())) {
          buys.add(order);
        } else {
          sells.add(order);
        }
      }

      BigDecimal totalProfit = BigDecimal.ZERO;
      double totalProfitRate = 0;
      int tradePairs = 0;

      // 매도 데이터를 기준으로 수익률 측정
      for (ClosedOrder sell : sells) {
        for (Iterator<ClosedOrder> it = buys.iterator(); it.hasNext(); ) {
          ClosedOrder buy = it.next();
          if (buy.getCreatedAt().isBefore(sell.getCreatedAt())) {
            BigDecimal profit = sell.getPrice().subtract(buy.getPrice());
            double profitRate = profit.divide(buy.getPrice(), RoundingMode.HALF_UP).doubleValue() * 100;
            totalProfit = totalProfit.add(profit);
            totalProfitRate += profitRate;
            tradePairs++;
            it.remove();
            break;  // Move to the next sell order
          }
        }
      }

      BigDecimal averageProfit = totalProfit; // 총 이익
      double averageProfitRate = tradePairs > 0 ? totalProfitRate / tradePairs : 0; // 평균 수익률

      System.out.println("Market: " + market);
      System.out.println("Total Profit: " + averageProfit);
      System.out.println("Average Profit Rate: " + averageProfitRate + "%");

      saveClosedOrderReport(market, averageProfit, averageProfitRate);
    }
  }

  public void saveClosedOrderReport(String market, BigDecimal averageProfit, Double profit) {
    ClosedOrderReport report = new ClosedOrderReport();
    report.setMarket(market);
    report.setAmount(averageProfit);
    report.setProfit(profit);
    closedOrderReportRepository.save(report);
  }
}
