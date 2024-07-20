package my.trader.coin.controller;

import my.trader.coin.dto.order.OrderManualRequestDto;
import my.trader.coin.enums.TradeType;
import my.trader.coin.service.UpbitService;
import my.trader.coin.util.IdentifierGenerator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 매수/매도 등 거래소에서 사용할 수 있는 기능을 수동 조작할 수 있게 해주는 컨트롤러.
 */
@RestController
public class ManualController {
  private final UpbitService upbitService;

  public ManualController(UpbitService upbitService) {
    this.upbitService = upbitService;
  }

  /**
   * 매수 수동 조작.
   */
  @PostMapping("/buy")
  public boolean buy(@RequestBody OrderManualRequestDto orderManualRequestDto) {
    Long userId = orderManualRequestDto.getUserId();

    String identifier = IdentifierGenerator.generateUniqueIdentifier(userId, TradeType.BUY.getName());

    return upbitService.executeBuyOrder(
          orderManualRequestDto.getTickerSymbol(),
          orderManualRequestDto.getPrice(),
          orderManualRequestDto.getQuantity(),
          identifier,
          orderManualRequestDto.getSimulationMode()
    );
  }
}
