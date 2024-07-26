package my.trader.coin.dto.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CancelOrderResponseDto {
  @JsonProperty("uuid")
  private String uuid; // 주문의 고유 아이디

  @JsonProperty("side")
  private String side; // 주문 종류

  @JsonProperty("ord_type")
  private String ordType; // 주문 방식

  @JsonProperty("price")
  private Double price; // 주문 당시 화폐 가격

  @JsonProperty("state")
  private String state; // 주문 상태

  @JsonProperty("market")
  private String market; // 마켓의 유일키

  @JsonProperty("created_at")
  private String createdAt; // 주문 생성 시간

  @JsonProperty("volume")
  private Double volume; // 사용자가 입력한 주문 양

  @JsonProperty("remaining_volume")
  private Double remainingVolume; // 체결 후 남은 주문 양

  @JsonProperty("reserved_fee")
  private Double reservedFee; // 수수료로 예약된 비용

  @JsonProperty("remaining_fee")
  private Double remainingFee; // 남은 수수료

  @JsonProperty("paid_fee")
  private Double paidFee; // 사용된 수수료

  @JsonProperty("locked")
  private Double locked; // 거래에 사용 중인 비용

  @JsonProperty("executed_volume")
  private Double executedVolume; // 체결된 양

  @JsonProperty("trades_count")
  private Integer tradesCount; // 해당 주문에 걸린 체결 수
}
