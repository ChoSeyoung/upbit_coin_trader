package my.trader.coin.dto.exchange;

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
  private String market;

  @Builder.Default
  private String state = "wait";

  @Builder.Default
  private List<String> states = List.of("wait");

  @Builder.Default
  private Integer page = 1;

  @Builder.Default
  private Integer limit = 100;

  @Builder.Default
  private String orderBy = "desc";
}
