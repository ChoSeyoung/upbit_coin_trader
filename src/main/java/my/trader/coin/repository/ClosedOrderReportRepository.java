package my.trader.coin.repository;

import my.trader.coin.model.ClosedOrderReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosedOrderReportRepository extends JpaRepository<ClosedOrderReport, Long> {
  ClosedOrderReport findTopByOrderByReportDateDescReportHourDesc();
}
