package my.trader.coin.repository;

import java.time.LocalDateTime;
import java.util.List;
import my.trader.coin.model.ClosedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClosedOrderRepository extends JpaRepository<ClosedOrder, String> {
  List<ClosedOrder> findByMarketAndCreatedAtBetween(String market, LocalDateTime start, LocalDateTime end);


  @Query("SELECT DISTINCT market FROM ClosedOrder")
  List<String> findDistinctMarkets();
}
