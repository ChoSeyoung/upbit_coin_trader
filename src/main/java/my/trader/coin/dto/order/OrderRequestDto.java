package my.trader.coin.dto.order;

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
  @NotNull(message = "Market is required")
  private String market;

  @NotNull(message = "Side is required")
  @Pattern(regexp = "^(bid|ask)$",
        message = "Side must be either 'bid' or 'ask'")
  private String side;

  @Positive(message = "Volume must be greater than zero")
  private Double volume;

  @Positive(message = "Price must be greater than zero")
  private Double price;

  @NotNull(message = "Order type is required")
  @Pattern(regexp = "^(limit|price|market|best)$",
        message = "Order type must be one of 'limit', 'price', 'market', or 'best'")
  private String ordType;

  private String identifier;

  @Pattern(regexp = "^(ioc|fok)?$",
        message = "Time in force must be either 'ioc' or 'fok'")
  private String timeInForce;
}