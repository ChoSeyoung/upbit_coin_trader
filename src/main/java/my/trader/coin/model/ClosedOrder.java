package my.trader.coin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "closed_orders")
@Getter
@Setter
public class ClosedOrder {
  @Id
  private String uuid;
  private String side;
  private String ordType;
  private BigDecimal price;
  private String state;
  private String market;
  private OffsetDateTime createdAt;
  private Double volume;
  private Double remainingVolume;
  private BigDecimal reservedFee;
  private BigDecimal remainingFee;
  private BigDecimal paidFee;
  private Double locked;
  private BigDecimal executedVolume;
  private BigDecimal executedFunds;
  private Integer tradesCount;
  private String timeInForce;
}
