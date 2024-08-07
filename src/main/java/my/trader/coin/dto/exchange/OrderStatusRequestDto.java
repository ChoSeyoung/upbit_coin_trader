package my.trader.coin.dto.exchange;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderStatusRequestDto 는 id로 주문리스트 조회하기 위한 데이터 전송 객체힙니다.
 * <a href="https://docs.upbit.com/reference/id%EB%A1%9C-%EC%A3%BC%EB%AC%B8-%EC%A1%B0%ED%9A%8C">...</a>
 * 주의사항 : uuids 또는 identifiers 중 한 가지 필드는 필수이며, 두 가지 필드를 함께 사용할 수 없습니다
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusRequestDto {
  private String market;
  private List<String> uuids;
  private List<String> identifiers;
  private String orderBy = "desc";
}
