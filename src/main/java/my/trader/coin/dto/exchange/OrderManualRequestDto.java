package my.trader.coin.dto.exchange;

import lombok.Data;

@Data
public class OrderManualRequestDto {
  private String side;
  private String market;
  private Double price;
  private Double quantity;
}
