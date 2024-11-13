package my.trader.coin.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Setter;
import my.trader.coin.enums.MarketCode;
import org.springframework.stereotype.Component;

/**
 * AppConfig 클래스는 암호화폐 거래 애플리케이션의 전역 설정 값을 관리하는 구성 클래스입니다.
 * 해당 클래스는 거래와 관련된 여러 가지 설정 값을 초기화하며, 주입된 값에 따라
 * 동작을 변경할 수 있습니다.
 */
@Component
public class AppConfig {
  @Setter
  public static double upbitMarketIndexRatio;
  @Setter
  public static List<String> scheduledMarket;
  @Setter
  public static boolean holdTrade;

  public static boolean wholeSellWhenProfit;
  public static double exchangeFeeRatio;
  public static double minOrderAmount;
  public static double minBuyAmount;
  public static double minSellAmount;
  public static double takeProfitPercentage;

  static {
    upbitMarketIndexRatio = 0.0;
    scheduledMarket = new ArrayList<>(
          Arrays.asList(MarketCode.KRW_BTC.getSymbol(), MarketCode.KRW_ETH.getSymbol(),
                MarketCode.KRW_XRP.getSymbol()));
    holdTrade = true;

    wholeSellWhenProfit = true;
    exchangeFeeRatio = 1.0005;
    minOrderAmount = 10000;
    minBuyAmount = 10000;
    minSellAmount = 10000;
    takeProfitPercentage = 0.5;
  }
}
