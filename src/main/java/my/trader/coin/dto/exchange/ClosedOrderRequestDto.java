package my.trader.coin.dto.exchange;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosedOrderRequestDto {

  @NotEmpty(message = "Market ID cannot be empty")
  private String market;

  @NotEmpty(message = "State cannot be empty")
  private String state;

  @NotEmpty(message = "startTime cannot be empty")
  private String startTime;

  @NotEmpty(message = "endTime cannot be empty")
  private String endTime;

  private String limit;

  @NotEmpty(message = "Order by cannot be empty")
  private String orderBy;
}

