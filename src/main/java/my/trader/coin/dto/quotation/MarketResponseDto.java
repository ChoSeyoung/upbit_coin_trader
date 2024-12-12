package my.trader.coin.dto.quotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 업비트에서 거래 가능한 종목 목록.
 */
@Data
public class MarketResponseDto {
  // 업비트에서 제공중인 시장 정보 (ex: KRW-BTC)
  @JsonProperty("market")
  private String market;

  // 거래 대상 디지털 자산 한글명
  @JsonProperty("korean_name")
  private String koreanName;

  // 거래 대상 디지털 자산 영문명
  @JsonProperty("english_name")
  private String englishName;

  // 업비트 시장경보
  @JsonProperty("market_event")
  private MarketEvent marketEvent;

  /**
   * 업비트 시장경보.
   */
  @Data
  public static class MarketEvent {

    // 유의종목 지정 여부
    @JsonProperty("warning")
    private boolean warning;

    // 주의 경보 타입
    @JsonProperty("caution")
    private Caution caution;

    /**
     * 주의 경보 타입.
     */
    @Data
    public static class Caution {

      // 가격 급등락 경보 발령 여부
      @JsonProperty("PRICE_FLUCTUATIONS")
      private boolean priceFluctuations;

      // 거래량 급등 경보 발령 여부
      @JsonProperty("TRADING_VOLUME_SOARING")
      private boolean tradingVolumeSoaring;

      // 입금량 급등 경보 발령 여부
      @JsonProperty("DEPOSIT_AMOUNT_SOARING")
      private boolean depositAmountSoaring;

      // 가격 차이 경보 발령 여부
      @JsonProperty("GLOBAL_PRICE_DIFFERENCES")
      private boolean globalPriceDifferences;

      // 소수 계정 집중 경보 발령 여부
      @JsonProperty("CONCENTRATION_OF_SMALL_ACCOUNTS")
      private boolean concentrationOfSmallAccounts;
    }
  }
}
