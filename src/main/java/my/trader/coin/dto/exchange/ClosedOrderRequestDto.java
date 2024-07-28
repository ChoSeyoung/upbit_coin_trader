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
  private String states;

//  @NotEmpty(message = "States cannot be empty")
//  private List<String> states;

  @NotEmpty(message = "startTime cannot be empty")
  private String startTime;

  @NotEmpty(message = "endTime cannot be empty")
  private String endTime;

  @Min(value = 1, message = "Limit must be at least 1")
  @Max(value = 1000, message = "Limit must be at most 100")
  private Integer limit;

  @NotEmpty(message = "Order by cannot be empty")
  private String orderBy;
}

