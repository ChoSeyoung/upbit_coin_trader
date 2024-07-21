package my.trader.coin.dto.order;

import lombok.Data;

@Data
public class OrderStatusResponseDto {
  private String uuid;
  private String side;
  private String ordType;
  private Double price;
  private String state;
  private String market;
  private String createdAt;
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
