package my.trader.coin.dto.quotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MarketResponseDto {
  @JsonProperty("market")
  private String market;

  @JsonProperty("korean_name")
  private String koreanName;

  @JsonProperty("english_name")
  private String englishName;

  @JsonProperty("market_event")
  private MarketEvent marketEvent;

  @Data
  public static class MarketEvent {

    @JsonProperty("warning")
    private boolean warning;

    @JsonProperty("caution")
    private Caution caution;

    @Data
    public static class Caution {

      @JsonProperty("PRICE_FLUCTUATIONS")
      private boolean priceFluctuations;

      @JsonProperty("TRADING_VOLUME_SOARING")
      private boolean tradingVolumeSoaring;

      @JsonProperty("DEPOSIT_AMOUNT_SOARING")
      private boolean depositAmountSoaring;

      @JsonProperty("GLOBAL_PRICE_DIFFERENCES")
      private boolean globalPriceDifferences;

      @JsonProperty("CONCENTRATION_OF_SMALL_ACCOUNTS")
      private boolean concentrationOfSmallAccounts;
    }
  }
}
