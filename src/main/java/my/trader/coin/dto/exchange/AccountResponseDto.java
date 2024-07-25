package my.trader.coin.dto.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccountResponseDto {
  @JsonProperty("currency")
  private String currency;

  @JsonProperty("balance")
  private Double balance;

  @JsonProperty("locked")
  private Double locked;

  @JsonProperty("avg_buy_price")
  private Double avgBuyPrice;

  @JsonProperty("avg_buy_price_modified")
  private Boolean avgBuyPriceModified;

  @JsonProperty("unit_currency")
  private String unitCurrency;
}
