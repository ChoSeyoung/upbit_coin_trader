package my.trader.coin.scheduler;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import my.trader.coin.dto.exchange.CancelOrderResponseDto;
import my.trader.coin.dto.exchange.OrderResponseDto;
import my.trader.coin.dto.quotation.TickerResponseDto;
import my.trader.coin.enums.*;
import my.trader.coin.model.Config;
import my.trader.coin.service.ConfigService;
import my.trader.coin.service.InventoryService;
import my.trader.coin.service.UpbitService;
import my.trader.coin.strategy.ScalpingStrategy;
import my.trader.coin.util.MathUtility;
import my.trader.coin.util.TimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * UpbitScheduler 는 정기적으로 시장 데이터를 가져오고 트레이딩 전략을 실행합니다.
 */
@Component
public class UpbitScheduler {
  private static final Logger logger = LoggerFactory.getLogger(UpbitScheduler.class);

  private int schedulerExecutedCount = 0;

  private final UpbitService upbitService;
  private final ScalpingStrategy scalpingStrategy;
  private final ConfigService configService;
  private final InventoryService inventoryService;

  // 콘솔 데이터 출력용 formatter
  DecimalFormat df = new DecimalFormat("#,##0.00");

  /**
   * this is constructor.
   *
   * @param upbitService     UpbitService
   * @param scalpingStrategy ScalpingStrategy
   * @param configService    ConfigService
   * @param inventoryService InventoryService
   */
  public UpbitScheduler(
        UpbitService upbitService,
        ScalpingStrategy scalpingStrategy,
        ConfigService configService,
        InventoryService inventoryService
  ) {
    this.upbitService = upbitService;
    this.scalpingStrategy = scalpingStrategy;
    this.configService = configService;
    this.inventoryService = inventoryService;
  }

  /**
   * 매시간마다 매수/매도 전략을 실행할 종목 선정합니다.
   */
  @Scheduled(cron = "0 0 * * * *") // 매 시간마다 실행
  public void runTechnicalAnalysis() {
    upbitService.selectScheduledMarket();
  }

  /**
   * 매 분마다 시장 데이터를 가져오고 스캘핑 전략을 실행합니다.
   */
  @Scheduled(cron = "1 * * * * *") // 매 분 1초에 실행
  public void runStrategy() {
    // 스케줄러 실행전 미체결된 매도 주문 취소 접수
    List<CancelOrderResponseDto> cancelSellOrders = upbitService.beforeTaskExecution();
    if (!cancelSellOrders.isEmpty()) {
      ColorfulConsoleOutput.printWithColor("매도 주문 잔여 수량 취소 작업 진행 완료",
            ColorfulConsoleOutput.GREEN);
    }

    try {
      Config scheduledMarketConfig =
            configService.getConfByName(CacheKey.SCHEDULED_MARKET.getKey());

      List<String> markets = Stream.of(scheduledMarketConfig.getVal().split(","))
            .collect(Collectors.toCollection(ArrayList::new));

      // 시장 데이터 조회
      List<TickerResponseDto> tickerDataList = upbitService.getTicker(markets);

      // 첫 번째 티커 데이터를 사용
      if (tickerDataList != null && !tickerDataList.isEmpty()) {
        for (TickerResponseDto tickerData : tickerDataList) {
          // 현재 마켓코드
          String market = tickerData.getMarket();
          // 현재 가격
          Double currentPrice = tickerData.getTradePrice();

          // 현재 가격 로깅
          ColorfulConsoleOutput.printWithColor(
                String.format("[%s] Current Price: %s", market, df.format(currentPrice)),
                ColorfulConsoleOutput.YELLOW
          );

          // 스캘핑 전략을 실행하여 매수 또는 매도 결정을 내림
          executeScalpingStrategy(market, currentPrice);

          ColorfulConsoleOutput.printWithColor("-------------", ColorfulConsoleOutput.CYAN);
          TimeUtility.sleep(1);
        }
      } else {
        logger.error("조회된 시장 데이터가 없습니다.");
      }
    } catch (Exception e) {
      logger.error("시장 데이터를 가져오는 중 오류 발생", e);
    } finally {
      TimeUtility.sleep(1);

      List<CancelOrderResponseDto> results = upbitService.afterTaskCompletion();
      if (!results.isEmpty()) {
        ColorfulConsoleOutput.printWithColor("매수 주문 잔여 수량 취소 작업 진행 완료",
              ColorfulConsoleOutput.GREEN);
      }

      ColorfulConsoleOutput.printWithColor(++schedulerExecutedCount + " set cleared",
            ColorfulConsoleOutput.CYAN);
    }
  }

  /**
   * 가져온 시장 데이터를 기반으로 스캘핑 전략을 실행합니다.
   *
   * @param market       마켓코드
   * @param currentPrice 자산의 현재 가격
   */
  private void executeScalpingStrategy(String market, double currentPrice) {
    // 현재 보유량 조회
    Double inventory = inventoryService.getQuantityByMarket(market);

    // 주문 수량 계산
    Double minimumOrderAmount = Double.parseDouble(
          configService.getConfByName(CacheKey.MIN_ORDER_AMOUNT.getKey()).getVal()
    );
    Double quantity = MathUtility.calculateMinimumOrderQuantity(minimumOrderAmount, currentPrice);

    // 매수 시그널 확인
    TimeUtility.sleep(1);
    Signal buySignal = scalpingStrategy.shouldBuy(market);
    // 매수 신호가 발생하면 매수 로직 실행
    if (buySignal.isBuySignal()) {
      OrderResponseDto result =
            upbitService.executeOrder(market, currentPrice, quantity,
                  UpbitType.ORDER_SIDE_BID.getType());

      // 매수 주문 실행 성공 후 처리 프로세스
      if (result != null) {
        ColorfulConsoleOutput.printWithColor(
              String.format("[%s] 매수 주문 발생: %s", market, df.format(currentPrice)),
              ColorfulConsoleOutput.RED
        );

        // 계좌정보 업데이트
        inventoryService.saveQuantity(TradeType.BUY, market, quantity);
      }
    }

    // 매도 시그널 확인
    TimeUtility.sleep(1);
    Signal sellSignal = scalpingStrategy.shouldSell(market, currentPrice, inventory);
    if (sellSignal.isSellSignal()) {
      // 전량 매도 플래그 활성화시 익절 시그널 발생되면 전량 매도
      if (sellSignal.equals(Signal.TAKE_PROFIT)) {
        Config config = configService.getConfByName(CacheKey.WHOLE_SELL_WHEN_PROFIT.getKey());
        boolean wholeSellWhenProfit = Boolean.parseBoolean(config.getVal());

        if (wholeSellWhenProfit) {
          quantity = inventory;
        }
      }

      // 매도 신호가 발생하면 매도 로직 실행
      // 매도금액은 최소주문 금액보다 많아야 처리 가능(업비트 정책)
      TimeUtility.sleep(1);
      if (currentPrice * quantity >= minimumOrderAmount) {
        OrderResponseDto result =
              upbitService.executeOrder(market, currentPrice, quantity,
                    UpbitType.ORDER_SIDE_ASK.getType());

        // 매도 주문 실행 성공 후 처리 프로세스
        if (result != null) {
          ColorfulConsoleOutput.printWithColor(
                String.format("[%s] 매도 주문 발생: %s", market,
                      df.format(currentPrice)),
                ColorfulConsoleOutput.BLUE
          );

          // 계좌정보 업데이트
          inventoryService.saveQuantity(TradeType.SELL, market, quantity);
        }
      }
    }
  }
}
