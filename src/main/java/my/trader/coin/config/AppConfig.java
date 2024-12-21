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
  // UBMI(UpBit Market Index)
  @Setter
  public static double upbitMarketIndexRatio;
  // 매수/매도 예정 종목
  @Setter
  public static List<String> scheduledMarket;
  public static List<String> initScheduledMarket;
  public static String activatedMarketSelectStrategy;

  // 수익실현 시 전체 물량 매도 여부
  public static boolean wholeSellWhenProfit;

  // 거래대금 상위 항목 동적 종목 추가 여부
  public static boolean includeTopTradingStocks;

  // 거래 수수료율 (업비트 정책)
  public static double exchangeFeeRatio;
  // 최소 주문금액 (업비트 정책)
  public static double minOrderAmount;

  // 최소 매수/매도금액 (1회당 개인 매수/매도 금액(스케줄러에서 재조정))
  // baseTradeAmount 는 매수/매도 금액에 대한 기준점을 제시합니다.
  // minTradeAmount 는 baseTradeAmount 에 UBMI 10 지수를 고려하여 최종적으로 거래 1회당 발생될 금액을 제시합니다.
  public static double baseTradeAmount;
  public static double minTradeAmount;

  // 익절율
  public static double takeProfitPercentage;

  static {
    upbitMarketIndexRatio = 0.0;
    initScheduledMarket = new ArrayList<>(
          Arrays.asList(
                MarketCode.KRW_BTC.getSymbol(),
                MarketCode.KRW_ETH.getSymbol(),
                MarketCode.KRW_XRP.getSymbol()
          ));
    scheduledMarket = initScheduledMarket;
    activatedMarketSelectStrategy = "ubmi_10";

    wholeSellWhenProfit = true;
    includeTopTradingStocks = true;
    exchangeFeeRatio = 1.0005;
    minOrderAmount = 5001;
    baseTradeAmount = 500000;
    minTradeAmount = 500000;
    takeProfitPercentage = 0.3;
  }
}
