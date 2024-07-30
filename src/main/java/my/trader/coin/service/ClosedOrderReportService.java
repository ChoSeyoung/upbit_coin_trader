package my.trader.coin.service;

import my.trader.coin.model.ClosedOrder;
import my.trader.coin.model.ClosedOrderReport;
import my.trader.coin.repository.ClosedOrderReportRepository;
import my.trader.coin.repository.ClosedOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClosedOrderReportService {
  private final ClosedOrderRepository closedOrderRepository;
  private final ClosedOrderReportRepository closedOrderReportRepository;

  public ClosedOrderReportService(ClosedOrderRepository closedOrderRepository,
                                  ClosedOrderReportRepository closedOrderReportRepository) {
    this.closedOrderRepository = closedOrderRepository;
    this.closedOrderReportRepository = closedOrderReportRepository;
  }

  @Transactional
  public void generateHourlyReport() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime start = now.minusHours(1).withMinute(0).withSecond(0).withNano(0);
    LocalDateTime end = start.withMinute(59).withSecond(59).withNano(999999999);

    List<String> markets = closedOrderRepository.findDistinctMarkets();

    for (String market : markets) {
      double profit = calculateProfit(market, start, end);

      ClosedOrderReport report = new ClosedOrderReport();
      report.setMarket(market);
      report.setReportDate(start.toLocalDate());
      report.setReportHour(start.getHour());
      report.setProfit(profit);

      closedOrderReportRepository.save(report);
    }
  }

  public double calculateProfit(String market, LocalDateTime start, LocalDateTime end) {
    List<ClosedOrder> orders =
          closedOrderRepository.findByMarketAndCreatedAtBetween(market, start, end);

    double totalExecutedFunds = orders.stream().mapToDouble(ClosedOrder::getExecutedFunds).sum();
    double totalVolume = orders.stream().mapToDouble(ClosedOrder::getExecutedVolume).sum();

    if (totalVolume == 0) return 0;
    double averagePrice = totalExecutedFunds / totalVolume;

    double closingPrice = orders.stream()
          .filter(order -> !order.getCreatedAt().isAfter(end))
          .mapToDouble(ClosedOrder::getPrice)
          .findFirst()
          .orElse(averagePrice);

    double openingPrice = orders.stream()
          .filter(order -> !order.getCreatedAt().isBefore(start))
          .mapToDouble(ClosedOrder::getPrice)
          .findFirst()
          .orElse(averagePrice);

    return (closingPrice - openingPrice) / openingPrice * 100;
  }
}
