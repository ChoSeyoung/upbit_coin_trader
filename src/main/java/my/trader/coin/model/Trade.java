package my.trader.coin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * [trades] 테이블 Entity.
 */
@Entity
@Table(name = "trades")
@Getter
@Setter
public class Trade {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String tickerSymbol; // EX: KRW-BTC, KRW-XRP
  private String type; // BUY or SELL
  private BigDecimal price;
  private Double quantity;
  private LocalDateTime timestamp;
  private Boolean simulationMode;
}
