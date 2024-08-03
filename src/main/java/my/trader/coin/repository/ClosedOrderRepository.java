package my.trader.coin.repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import my.trader.coin.model.ClosedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClosedOrderRepository extends JpaRepository<ClosedOrder, String> {
  List<ClosedOrder> findByMarketAndCreatedAtBetween(String market, OffsetDateTime start, OffsetDateTime end);

  @Query("SELECT DISTINCT market FROM ClosedOrder")
  List<String> findDistinctMarkets();

  @Query("SELECT MAX(c.createdAt) FROM ClosedOrder c")
  OffsetDateTime findLastCreatedAt();

  @Query("SELECT MIN(c.createdAt) FROM ClosedOrder c")
  OffsetDateTime findEarliestCreatedAt();
}
