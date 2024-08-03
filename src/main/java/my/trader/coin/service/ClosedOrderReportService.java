package my.trader.coin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
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
  public void generateHourlyReport(boolean isScheduler) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(9));

    OffsetDateTime start;
    OffsetDateTime end = now.withMinute(59).withSecond(59).withNano(999999999);

    if (isScheduler) {
      ClosedOrderReport lastReport =
            closedOrderReportRepository.findTopByOrderByReportDateDescReportHourDesc();
      if (lastReport != null) {
        // 이 시간은 본래 시간에 9시간을 추가한 것이 아니라, 설정한 시간이 이미 UTC+9 시간대를 기준으로 한다는 것입니다.
        start = OffsetDateTime.of(lastReport.getReportDate().atTime(lastReport.getReportHour(), 0),
              ZoneOffset.ofHours(9)).plusHours(1);
      } else {
        start = closedOrderRepository.findEarliestCreatedAt();
      }
    } else {
      start = closedOrderRepository.findEarliestCreatedAt();
    }

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

  private OffsetDateTime findEarliestCreatedAt() {
    return closedOrderRepository.findEarliestCreatedAt();
  }

  public double calculateProfit(String market, OffsetDateTime start, OffsetDateTime end) {
    List<ClosedOrder> orders =
          closedOrderRepository.findByMarketAndCreatedAtBetween(market, start, end);

    BigDecimal totalExecutedFunds = orders.stream()
          .map(ClosedOrder::getExecutedFunds)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalVolume = orders.stream()
          .map(ClosedOrder::getExecutedVolume)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalVolume.compareTo(BigDecimal.ZERO) == 0) return 0;
    BigDecimal averagePrice = totalExecutedFunds.divide(totalVolume, RoundingMode.HALF_UP);

    BigDecimal closingPrice = orders.stream()
          .filter(order -> !order.getCreatedAt().isAfter(end))
          .map(ClosedOrder::getPrice)
          .findFirst()
          .orElse(averagePrice);

    BigDecimal openingPrice = orders.stream()
          .filter(order -> !order.getCreatedAt().isBefore(start))
          .map(ClosedOrder::getPrice)
          .findFirst()
          .orElse(averagePrice);

    BigDecimal profitPercentage = closingPrice.subtract(openingPrice)
          .divide(openingPrice, RoundingMode.HALF_UP)
          .multiply(new BigDecimal("100"));

    return profitPercentage.doubleValue();
  }
}
