package my.trader.coin.scheduler;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import my.trader.coin.dto.exchange.OrderResponseDto;
import my.trader.coin.dto.quotation.TickerResponseDto;
import my.trader.coin.enums.*;
import my.trader.coin.model.Config;
import my.trader.coin.service.ConfigService;
import my.trader.coin.service.InventoryService;
import my.trader.coin.service.UpbitService;
import my.trader.coin.strategy.ScalpingStrategy;
import my.trader.coin.util.Thales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * UpbitScheduler 는 정기적으로 시장 데이터를 가져오고 트레이딩 전략을 실행합니다.
 */
@Component
public class UpbitScheduler {
  private static final Logger logger = LoggerFactory.getLogger(UpbitScheduler.class);

  // 거래수수료
  @Value("${upbit.ratio.exchange}")
  private double exchangeFeeRatio;

  // 시뮬레이션 모드 플래그
  @Value("${simulation.mode}")
  private boolean simulationMode;

  @Value("${upbit.minimum.order.amount}")
  private double minimumOrderAmount;

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
   * 매 분마다 시장 데이터를 가져오고 스캘핑 전략을 실행합니다.
   */
  @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
  public void fetchMarketData() {
    try {
      Config scheduledMarketConfig = configService.getConfByName(CacheKey.SCHEDULED_MARKET.getKey());

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
        }
      } else {
        logger.error("조회된 시장 데이터가 없습니다.");
      }
    } catch (Exception e) {
      logger.error("시장 데이터를 가져오는 중 오류 발생", e);
    } finally {
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
    double quantity = Thales.calculateMinimumOrderQuantity(minimumOrderAmount, currentPrice);

    // 매수 시그널 확인
    Signal buySignal = scalpingStrategy.shouldBuy(market);
    // 매수 신호가 발생하면 매수 로직 실행
    if (buySignal.isBuySignal()) {
      OrderResponseDto result =
            upbitService.executeOrder(market, currentPrice, quantity, UpbitType.ORDER_SIDE_BID.getType());

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

    Signal sellSignal = scalpingStrategy.shouldSell(market);
    if (sellSignal.isSellSignal() && inventory > 0) {
      // 전량 매도 플래그 활성화시 익절 시그널 발생되면 전량 매도
      if (sellSignal.equals(Signal.TAKE_PROFIT)) {
        Config config = configService.getConfByName(CacheKey.WHOLE_SELL_WHEN_PROFIT.getKey());
        boolean wholeSellWhenProfit = Boolean.parseBoolean(config.getVal());

        if (wholeSellWhenProfit) {
          quantity = inventory;
        }
      }

      // 매도 신호가 발생하면 매도 로직 실행
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
