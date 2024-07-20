package my.trader.coin.dto.order;

import lombok.Data;

@Data
public class OrderManualRequestDto {
  private Long userId;
  private String tickerSymbol;
  private Double price;
  private Double quantity;
  private Boolean simulationMode;
}
