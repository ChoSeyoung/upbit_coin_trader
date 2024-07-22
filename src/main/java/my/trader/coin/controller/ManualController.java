package my.trader.coin.controller;

import java.util.Map;
import my.trader.coin.dto.order.OrderManualRequestDto;
import my.trader.coin.dto.order.OrderResponseDto;
import my.trader.coin.enums.UpbitType;
import my.trader.coin.service.UpbitService;
import my.trader.coin.util.AuthorizationGenerator;
import org.springframework.web.bind.annotation.*;

/**
 * 매수/매도 등 거래소에서 사용할 수 있는 기능을 수동 조작할 수 있게 해주는 컨트롤러.
 */
@RestController
public class ManualController {
  private final UpbitService upbitService;
  private final AuthorizationGenerator authorizationGenerator;

  public ManualController(UpbitService upbitService,
                          AuthorizationGenerator authorizationGenerator) {
    this.upbitService = upbitService;
    this.authorizationGenerator = authorizationGenerator;
  }

  /**
   * jwt 발급.
   *
   * @param requestBody 파라미터
   * @return jwt
   */
  @PostMapping("/jwt")
  public String getJwtWithParameter(
        @RequestBody(required = false) Map<String, Object> requestBody) {
    if (requestBody == null) {
      return authorizationGenerator.generateTokenWithoutParameter();
    } else {
      return authorizationGenerator.generateTokenWithParameter(requestBody);
    }
  }

  /**
   * 매수/매도 수동 조작.
   */
  @PostMapping("/order")
  public OrderResponseDto buy(@RequestBody OrderManualRequestDto orderManualRequestDto) {
    if (orderManualRequestDto.getSide().equals(UpbitType.ORDER_SIDE_BID.getType())) {
      return upbitService.executeBuyOrder(
            orderManualRequestDto.getTickerSymbol(),
            orderManualRequestDto.getPrice(),
            orderManualRequestDto.getQuantity()
      );
    } else {
      return upbitService.executeSellOrder(
            orderManualRequestDto.getTickerSymbol(),
            orderManualRequestDto.getPrice(),
            orderManualRequestDto.getQuantity()
      );
    }

  }
}