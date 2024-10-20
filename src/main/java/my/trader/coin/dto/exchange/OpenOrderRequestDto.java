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
  private String state = "wait";
  private List<String> states = List.of("wait");
  private Integer page = 1;
  private Integer limit = 100;
  private String orderBy = "desc";
}
