package my.trader.coin.dto.quotation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleRequestDto {
  @NotBlank(message = "Market code cannot be blank")
  private String market;

  @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}([T ])\\d{2}:\\d{2}:\\d{2}(Z|\\+\\d{2}:\\d{2})?$",
        message = "Invalid date format")
  private String to;

  @NotNull(message = "Count cannot be null")
  @Min(value = 1, message = "Count must be at least 1")
  @Max(value = 200, message = "Count must be at most 200")
  private Integer count;
}
