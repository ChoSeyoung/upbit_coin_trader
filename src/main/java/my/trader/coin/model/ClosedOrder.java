package my.trader.coin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
  private Double price;
  private String state;
  private String market;
  private Date createdAt;
  private Double volume;
  private Double remainingVolume;
  private Double reservedFee;
  private Double remainingFee;
  private Double paidFee;
  private Double locked;
  private Double executedVolume;
  private Double executedFunds;
  private Integer tradesCount;
  private String timeInForce;
}
