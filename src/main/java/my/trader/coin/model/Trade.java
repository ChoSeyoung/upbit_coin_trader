package my.trader.coin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
  private Double price;
  private Double quantity;
  private Double exchangeFee;
  private LocalDateTime timestamp;
  private String identifier;
  private Boolean simulationMode;
  private Boolean isSigned;
}
