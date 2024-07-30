package my.trader.coin.dto.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 해당 파일 수정 시 ClosedOrder 엔티티도 같이 수정해주어야함
 */
@Data
public class ClosedOrderResponseDto {
  @JsonProperty("uuid")
  private String uuid;

  @JsonProperty("side")
  private String side;

  @JsonProperty("ord_type")
  private String ordType;

  @JsonProperty("price")
  private Double price;

  @JsonProperty("state")
  private String state;

  @JsonProperty("market")
  private String market;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @JsonProperty("volume")
  private Double volume;

  @JsonProperty("remaining_volume")
  private Double remainingVolume;

  @JsonProperty("reserved_fee")
  private Double reservedFee;

  @JsonProperty("remaining_fee")
  private Double remainingFee;

  @JsonProperty("paid_fee")
  private Double paidFee;

  @JsonProperty("locked")
  private Double locked;

  @JsonProperty("executed_volume")
  private Double executedVolume;

  @JsonProperty("executed_funds")
  private Double executedFunds;

  @JsonProperty("trades_count")
  private Integer tradesCount;

  @JsonProperty("time_in_force")
  private String timeInForce;
}

