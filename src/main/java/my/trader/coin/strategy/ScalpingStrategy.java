package my.trader.coin.strategy;

import java.util.List;
import java.util.Optional;
import my.trader.coin.dto.exchange.AccountResponseDto;
import my.trader.coin.enums.CacheKey;
import my.trader.coin.enums.ColorfulConsoleOutput;
import my.trader.coin.enums.Signal;
import my.trader.coin.service.ConfigService;
import my.trader.coin.service.UpbitService;
import my.trader.coin.util.MathUtility;
import org.springframework.stereotype.Service;

/**
 * 스캘핑 전략을 이용하여 매매를 진행합니다.
 */
@Service
public class ScalpingStrategy {

  private final UpbitService upbitService;
  private final ConfigService configService;

  public ScalpingStrategy(UpbitService upbitService, ConfigService configService) {
    this.upbitService = upbitService;
    this.configService = configService;
  }

  /**
   * 매수 의사결정.
   *
   * @param market 마켓코드
   * @return 매수 결정시 true
   */
  public Signal shouldBuy(String market) {
    ColorfulConsoleOutput.printWithColor("매수 의사결정을 위한 가격 확인", ColorfulConsoleOutput.RED);

    double rsi = upbitService.calculateRelativeStrengthIndex(market, 14);

    // RSI 로깅
    ColorfulConsoleOutput.printWithColor(String.format("[%s] RSI: %s", market, rsi),
          ColorfulConsoleOutput.RED);

    return (rsi < 30) ? Signal.BUY : Signal.NO_ACTION;
  }

  /**
   * 매도 의사결정.
   *
   * @param market 마켓코드
   * @param currentPrice 매도가
   * @param inventory 보유량
   * @return 매도 결정시 true
   */
  public Signal shouldSell(String market, Double currentPrice, Double inventory) {
    ColorfulConsoleOutput.printWithColor("매도 의사결정을 위한 가격 확인", ColorfulConsoleOutput.BLUE);

    // TODO. RSI 기준으로 매도 작업을 진행하게 되는 경우 아래 코드를 참고하세요
    // List<Double> closePrices = upbitService.getClosePrices(market, 15);
    // double rsi = MathUtility.calculateRsi(closePrices, 14);

    String currency = market.split("-")[1];
    List<AccountResponseDto> accounts = upbitService.getAccount();

    // 전체 예산 계산
    double budget = accounts.stream()
          .mapToDouble(AccountResponseDto::getBalance)
          .sum();
    // 목표 암호화폐 지정
    Optional<AccountResponseDto> target = accounts.stream()
          .filter(account -> account.getCurrency().equalsIgnoreCase(currency))
          .findFirst();

    if (target.isPresent()) {
      AccountResponseDto account = target.get();

      // 익절 목표 퍼센티지
      double targetProfit = Double.parseDouble(
            configService.getConfByName(CacheKey.TAKE_PROFIT_PERCENTAGE.getKey()).getVal()
      );
      // 거래가 길어질것을 대비하여 매수한 금액이 전체예산의 10분의 1이 넘을 경우
      // 목표 수익률을 반으로 줄여 안전하게 수익을 창출한다.
      if (account.getAvgBuyPrice() >= (budget * 0.1)) {
        targetProfit = targetProfit * 0.5;
      }
      // 거래소 수수료
      double exchangeFeeRatio = Double.parseDouble(
            configService.getConfByName(CacheKey.EXCHANGE_FEE_RATIO.getKey()).getVal()
      );

      // 현재 수익률 계산
      double profitRate = ((currentPrice - account.getAvgBuyPrice()) / account.getAvgBuyPrice()) * 100;

      // 현재수익률/목표수익률 로깅
      ColorfulConsoleOutput.printWithColor(
            String.format("현재수익률/목표수익률 : %s/%s", profitRate, targetProfit),
            ColorfulConsoleOutput.BLUE);

      // RSI 70선 이상이면서 익절목표 금액에 도달한경우 매도 신호 발생
      return (profitRate > targetProfit) ? Signal.TAKE_PROFIT : Signal.NO_ACTION;
    }

    return Signal.NO_ACTION;
  }
}
