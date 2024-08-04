package my.trader.coin.repository;

import my.trader.coin.model.ClosedOrderReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ClosedOrderReportRepository extends JpaRepository<ClosedOrderReport, Long> {
  @Modifying
  @Transactional
  @Query(value = "TRUNCATE TABLE closed_orders_report", nativeQuery = true)
  void truncateTable();
}
