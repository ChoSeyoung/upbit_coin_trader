package my.trader.coin.strategy;

import java.util.List;
import java.util.Optional;
import my.trader.coin.config.AppConfig;
import my.trader.coin.dto.exchange.AccountResponseDto;
import my.trader.coin.enums.ColorfulConsoleOutput;
import my.trader.coin.enums.Signal;
import my.trader.coin.service.UpbitService;
import org.springframework.stereotype.Service;

/**
 * 스캘핑 전략을 이용하여 매매를 진행합니다.
 */
@Service
public class ScalpingStrategy {

  private final UpbitService upbitService;

  public ScalpingStrategy(UpbitService upbitService) {
    this.upbitService = upbitService;
  }

  /**
   * 매수 의사결정.
   *
   * @param market 마켓코드
   * @return 매수 결정시 true
   */
  public Signal shouldBuy(String market) {
    ColorfulConsoleOutput.printWithColor(String.format("[%s] 매수 의사결정을 위한 가격 확인", market),
          ColorfulConsoleOutput.RED);

    double rsi = upbitService.calculateRelativeStrengthIndex(market, 14);

    // RSI 로깅
    ColorfulConsoleOutput.printWithColor(String.format("RSI: %s", rsi),
          ColorfulConsoleOutput.RED);

    // UBMI 인덱스를 활용한 매수 RSI 기준 값 변경
    double standardRsi = (AppConfig.upbitMarketIndexRatio > 10) ? 35 : 32;

    return (rsi <= standardRsi) ? Signal.BUY : Signal.NO_ACTION;
  }

  /**
   * 매도 의사결정.
   *
   * @param market             마켓코드
   * @param currentPrice       매도가
   * @return 매도 결정시 true
   */
  public Signal shouldSell(String market, Double currentPrice) {
    ColorfulConsoleOutput.printWithColor(String.format("[%s] 매도 의사결정을 위한 가격 확인", market),
          ColorfulConsoleOutput.BLUE);

    String currency = market.split("-")[1];
    List<AccountResponseDto> accounts = upbitService.getAccount();

    // 목표 암호화폐 지정
    Optional<AccountResponseDto> target = accounts.stream()
          .filter(account -> account.getCurrency().equalsIgnoreCase(currency))
          .findFirst();

    // 목표 암호화폐 확인시 매도 프로세스 실행
    if (target.isPresent()) {
      // 계좌 조회
      AccountResponseDto account = target.get();

      // 현재 보유 금액이 최소주문금액 이하 종목은 추가 진행하지 않음.
      double buyAmount = account.getAvgBuyPrice() * account.getBalance();
      if (buyAmount <= 5000) {
        // 현재수익률/목표수익률 로깅
        ColorfulConsoleOutput.printWithColor("최소 주문 금액 이하 종목 추가 진행 불가", ColorfulConsoleOutput.BLUE);
        return Signal.NO_ACTION;
      }

      // 익절 목표 퍼센티지 조회
      double targetProfit;
      if (buyAmount > 100000) {
        targetProfit = AppConfig.takeProfitPercentage * 0.6;
      } else {
        targetProfit = AppConfig.takeProfitPercentage;
      }

      // 거래소 수수료
      double exchangeFeeRatio = AppConfig.exchangeFeeRatio;

      // 현재 수익률 계산 : (현재가 - (평균매수가 * 1.0005)) / 평균매수가 * 100
      double profitRate = (currentPrice - (account.getAvgBuyPrice() * exchangeFeeRatio))
            / account.getAvgBuyPrice() * 100;

      // 현재수익률/목표수익률 로깅
      ColorfulConsoleOutput.printWithColor(
            String.format("현재수익률/목표수익률 : %.1f/%.1f", profitRate, targetProfit),
            ColorfulConsoleOutput.BLUE);

      double rsi = upbitService.calculateRelativeStrengthIndex(market, 14);

      // 손절목표 금액에 도달한경우 손절 신호 발생
      if (rsi >= 65 && profitRate < -2) {
        return Signal.STOP_LOSS;
      }

      // 익절목표 금액에 도달한경우 매도 신호 발생
      return (profitRate > targetProfit) ? Signal.TAKE_PROFIT : Signal.NO_ACTION;
    } else {
      ColorfulConsoleOutput.printWithColor("미보유 종목 매도 불가", ColorfulConsoleOutput.BLUE);
    }

    return Signal.NO_ACTION;
  }
}
