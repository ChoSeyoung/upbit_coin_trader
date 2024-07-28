package my.trader.coin.repository;

import my.trader.coin.model.ClosedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosedOrderRepository extends JpaRepository<ClosedOrder, String> {
}
