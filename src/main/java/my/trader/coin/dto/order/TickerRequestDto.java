package my.trader.coin.dto.order;

import java.util.List;
import lombok.Data;

@Data
public class TickerRequestDto {
  private List<String> markets;
}
