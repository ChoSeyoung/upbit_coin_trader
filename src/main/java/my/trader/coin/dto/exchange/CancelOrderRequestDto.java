package my.trader.coin.dto.exchange;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequestDto {
  @NotBlank(message = "UUID cannot be blank if provided")
  private String uuid;
  private String identifier;
}
