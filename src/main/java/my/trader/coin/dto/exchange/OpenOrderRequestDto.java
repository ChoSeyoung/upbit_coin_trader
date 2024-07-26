package my.trader.coin.dto.exchange;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenOrderRequestDto {

  @NotEmpty(message = "Market ID cannot be empty")
  private String market;

  @NotEmpty(message = "State cannot be empty")
  private String state = "wait";

  @NotNull(message = "States cannot be null")
  private List<String> states = List.of("wait");

  @Min(value = 1, message = "Page must be at least 1")
  private Integer page = 1;

  @Min(value = 1, message = "Limit must be at least 1")
  @Max(value = 100, message = "Limit must be at most 100")
  private Integer limit = 100;

  @NotEmpty(message = "Order by cannot be empty")
  private String orderBy = "desc";
}
