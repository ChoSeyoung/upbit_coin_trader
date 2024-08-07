package my.trader.coin.scheduler;

import java.text.DecimalFormat;
import java.util.*;
import my.trader.coin.config.AppConfig;
import my.trader.coin.dto.exchange.AccountResponseDto;
import my.trader.coin.dto.exchange.CancelOrderResponseDto;
import my.trader.coin.dto.exchange.OrderResponseDto;
import my.trader.coin.dto.quotation.TickerResponseDto;
import my.trader.coin.enums.*;
import my.trader.coin.service.UpbitService;
import my.trader.coin.strategy.ScalpingStrategy;
import my.trader.coin.util.MathUtility;
import my.trader.coin.util.TimeUtility;
import my.trader.coin.util.WebScraper;
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
  private final WebScraper webScraper;

  // 콘솔 데이터 출력용 formatter
  DecimalFormat df = new DecimalFormat("#,##0.00");

  /**
   * this is constructor.
   *
   * @param upbitService             UpbitService
   * @param scalpingStrategy         ScalpingStrategy
   */
  public UpbitScheduler(
        UpbitService upbitService,
        ScalpingStrategy scalpingStrategy,
        WebScraper webScraper
  ) {
    this.upbitService = upbitService;
    this.scalpingStrategy = scalpingStrategy;
    this.webScraper = webScraper;
  }

  /**
   * UBMI 지수 스크래핑.
   * 현재 업비트 제공하지 않음
   */
  @Scheduled(cron = "0 * * * * *")
  public void updateUpbitMarketIndexRatio() {
    webScraper.fetchUpbitMarketIndexRatio();
  }

  /**
   * 매 분마다 시장 데이터를 가져오고 스캘핑 전략을 실행합니다.
   */
  @Scheduled(cron = "0 * * * * *")
  public void runStrategy() {
    // 스케줄러 실행전 미체결된 매도 주문 취소 접수
    List<CancelOrderResponseDto> cancelSellOrders = upbitService.beforeTaskExecution();
    if (!cancelSellOrders.isEmpty()) {
      ColorfulConsoleOutput.printWithColor("매수/매도 주문 잔여 수량 취소 작업 진행 완료",
            ColorfulConsoleOutput.GREEN);
    }
    TimeUtility.sleep(1);
    // 매수 프로세스 실행
    runBuy();
    TimeUtility.sleep(1);
    // 매도 프로세스 실행
    runSell();
    TimeUtility.sleep(1);
    // 완료 로깅
    ColorfulConsoleOutput.printWithColor(++schedulerExecutedCount + " set cleared",
          ColorfulConsoleOutput.CYAN);
  }

  private void runBuy() {
    List<String> markets = AppConfig.scheduledMarket;

    // 주문 수량 계산
    Double minimumOrderAmount = AppConfig.minOrderAmount;

    // 시장 데이터 조회
    List<TickerResponseDto> tickerDataList = upbitService.getTicker(markets);

    if (tickerDataList != null && !tickerDataList.isEmpty()) {
      for (TickerResponseDto tickerData : tickerDataList) {

        // 마켓코드(ex: KRW-BTC)
        String market = tickerData.getMarket();

        // 매수 시그널 확인
        Signal buySignal = scalpingStrategy.shouldBuy(market);
        // 현재 가격
        Double currentPrice = tickerData.getTradePrice();

        // 매수 시그널 확인
        if (buySignal.isBuySignal()) {
          // 주문 수량 계산
          Double quantity =
                MathUtility.calculateMinimumOrderQuantity(minimumOrderAmount, currentPrice);

          OrderResponseDto result =
                upbitService.executeOrder(market, currentPrice, quantity,
                      UpbitType.ORDER_SIDE_BID.getType());

          // 매수 주문 실행 성공 후 처리 프로세스
          if (result != null) {
            ColorfulConsoleOutput.printWithColor(
                  String.format("[%s] 매수 주문 발생: %s", market, df.format(currentPrice)),
                  ColorfulConsoleOutput.RED
            );
          }
        }
        TimeUtility.sleep(1);
      }
    }
    upbitService.selectScheduledMarket();
  }

  private void runSell() {
    // 주문 수량 계산
    Double minimumOrderAmount = AppConfig.minOrderAmount;

    // 계좌 조회
    List<AccountResponseDto> accounts = upbitService.getAccount();

    // 보유 종목 리스트화
    List<String> markets = accounts.stream()
          .filter(account -> !"KRW".equals(account.getCurrency()))
          .map(account -> account.getUnitCurrency() + "-" + account.getCurrency())
          .toList();

    // 시장 데이터 조회
    List<TickerResponseDto> tickerDataList = upbitService.getTicker(markets);

    if (tickerDataList != null && !tickerDataList.isEmpty()) {
      for (TickerResponseDto tickerData : tickerDataList) {
        String market = tickerData.getMarket();

        // 현재 종목 조회
        Optional<AccountResponseDto> account = accounts.stream()
              .filter(x -> String.format("%s-%s", x.getUnitCurrency(), x.getCurrency())
                    .equals(market))
              .findFirst();

        if (account.isPresent()) {
          AccountResponseDto selectedAccount = account.get();

          // 현재 보유량 조회
          Double inventory = selectedAccount.getBalance();

          // 매도 시그널 확인
          Signal sellSignal =
                scalpingStrategy.shouldSell(market, tickerData.getTradePrice(), minimumOrderAmount);

          // 익절 시그널 발생시
          if (sellSignal.isSellSignal()) {
            // 주문 수량 계산
            Double quantity = MathUtility.calculateMinimumOrderQuantity(minimumOrderAmount,
                  tickerData.getTradePrice());

            // 전량 매도 플래그 활성화시 익절 시그널 발생되면 전량 매도
            if (sellSignal.equals(Signal.TAKE_PROFIT)) {
              if (AppConfig.wholeSellWhenProfit) {
                quantity = inventory;
              }
            }

            // 매도 신호가 발생하면 매도 로직 실행
            // 매도금액은 최소주문 금액보다 많아야 처리 가능(업비트 정책)
            if (tickerData.getTradePrice() * quantity >= minimumOrderAmount) {
              OrderResponseDto result =
                    upbitService.executeOrder(market, tickerData.getTradePrice(), quantity,
                          UpbitType.ORDER_SIDE_ASK.getType());

              // 매도 주문 실행 성공 후 처리 프로세스
              if (result != null) {
                ColorfulConsoleOutput.printWithColor(
                      String.format("[%s] 매도 주문 발생: %s", market,
                            df.format(tickerData.getTradePrice())),
                      ColorfulConsoleOutput.BLUE
                );
              }
            }
          }
        }
        TimeUtility.sleep(1);
      }
    }
    upbitService.selectScheduledMarket();
  }
}
