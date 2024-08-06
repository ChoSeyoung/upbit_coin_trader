package my.trader.coin.repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import my.trader.coin.model.ClosedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClosedOrderRepository extends JpaRepository<ClosedOrder, String> {
  @Query("SELECT DISTINCT market FROM ClosedOrder")
  List<String> findDistinctMarkets();

  @Query("SELECT MAX(createdAt) FROM ClosedOrder")
  OffsetDateTime findLastCreatedAt();

  List<ClosedOrder> findByMarket(String market);

  List<ClosedOrder> findByMarketOrderByCreatedAtAsc(String market);
}
