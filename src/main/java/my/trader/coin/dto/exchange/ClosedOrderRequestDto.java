package my.trader.coin.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosedOrderRequestDto {
  private String market;
  private String state;
  private String startTime;
  private String endTime;
  private String limit;
  private String orderBy;
}

