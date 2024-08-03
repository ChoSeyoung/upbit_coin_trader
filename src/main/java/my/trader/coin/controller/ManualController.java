package my.trader.coin.controller;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import my.trader.coin.dto.exchange.ClosedOrderResponseDto;
import my.trader.coin.service.ClosedOrderReportService;
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
  private final ClosedOrderReportService closedOrderReportService;

  public ManualController(UpbitService upbitService,
                          AuthorizationGenerator authorizationGenerator,
                          ClosedOrderReportService closedOrderReportService) {
    this.upbitService = upbitService;
    this.authorizationGenerator = authorizationGenerator;
    this.closedOrderReportService = closedOrderReportService;
  }

  /**
   * jwt 발급.
   *
   * @param requestBody 파라미터
   * @return jwt
   */
  @PostMapping("/init/jwt")
  public String getJwtWithParameter(
        @RequestBody(required = false) Map<String, Object> requestBody) {
    if (requestBody == null) {
      return authorizationGenerator.generateTokenWithoutParameter();
    } else {
      return authorizationGenerator.generateTokenWithParameter(requestBody);
    }
  }

  @PostMapping("/init/orders/closed")
  public List<ClosedOrderResponseDto> initClosedOrders(@RequestBody(required = false) String type) {
    return upbitService.initializeClosedOrders(type);
  }

  @PostMapping("/init/scheduled/market")
  public void initScheduledMarket() {
    upbitService.selectScheduledMarket();
  }

  @PostMapping("/init/scheduled/report")
  public void initScheduledReport() {
    closedOrderReportService.generateHourlyReport(false);
  }
}
