package my.trader.coin.strategy;

import java.util.List;
import java.util.Optional;
import my.trader.coin.dto.exchange.AccountResponseDto;
import my.trader.coin.enums.ColorfulConsoleOutput;
import my.trader.coin.enums.Signal;
import my.trader.coin.model.Config;
import my.trader.coin.service.ConfigService;
import my.trader.coin.service.UpbitService;
import my.trader.coin.util.Thales;
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

    List<Double> closePrices = upbitService.getClosePrices(market, 15);

    double rsi = Thales.calculateRsi(closePrices, 14);

    return (rsi < 25) ? Signal.BUY : Signal.NO_ACTION;
  }

  /**
   * 매도 의사결정.
   *
   * @param market 마켓코드
   * @return 매도 결정시 true
   */
  public Signal shouldSell(String market, Double currentPrice) {
    ColorfulConsoleOutput.printWithColor("매도 의사결정을 위한 가격 확인", ColorfulConsoleOutput.BLUE);

    List<Double> closePrices = upbitService.getClosePrices(market, 15);

    double rsi = Thales.calculateRsi(closePrices, 14);

    String currency = market.split("-")[1];
    List<AccountResponseDto> accounts = upbitService.getAccount();

    Optional<AccountResponseDto> target = accounts.stream()
          .filter(account -> account.getCurrency().equalsIgnoreCase(currency))
          .findFirst();

    if (target.isPresent()) {
      AccountResponseDto account = target.get();
      Double avgBuyPrice = account.getAvgBuyPrice();

      double profitRate = ((currentPrice - avgBuyPrice) / avgBuyPrice) * 100;

      double targetProfit = Double.parseDouble(
            configService.getConfByName("take_profit_percentage").getVal()
      );

      return (rsi > 75 || profitRate > targetProfit) ? Signal.TAKE_PROFIT : Signal.NO_ACTION;
    }
    return Signal.NO_ACTION;
  }
}
