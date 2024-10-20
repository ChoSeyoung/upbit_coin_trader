package my.trader.coin.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Setter;
import org.springframework.stereotype.Component;

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
    scheduledMarket = new ArrayList<>(Arrays.asList("KRW-BTC", "KRW-XRP", "KRW-ETH"));
    holdTrade = true;

    wholeSellWhenProfit = true;
    exchangeFeeRatio = 1.0005;
    minOrderAmount = 5100;
    minBuyAmount = 20000;
    minSellAmount = 20000;
    takeProfitPercentage = 0.5;
  }
}
