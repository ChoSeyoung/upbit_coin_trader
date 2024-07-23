package my.trader.coin.repository;

import java.util.List;
import my.trader.coin.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * trades table's Repository.
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
  List<Trade> findBySimulationModeFalseAndIsSignedFalseOrIsSignedIsNull();

  List<Trade> findByType(String type);

  List<Trade> findByTypeAndTickerSymbol(String type, String tickerSymbol);

  Trade findByIdentifier(String identifier);

}
