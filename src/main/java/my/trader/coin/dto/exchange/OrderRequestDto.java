package my.trader.coin.dto.exchange;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderRequestDTO 는 주문 요청을 위한 데이터 전송 객체입니다.
 * <a href="https://docs.upbit.com/reference/%EC%A3%BC%EB%AC%B8%ED%95%98%EA%B8%B0">...</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
  private String market;
  private String side;
  private Double volume;
  private Double price;
  private String ordType;
  private String identifier;
  private String timeInForce;
}