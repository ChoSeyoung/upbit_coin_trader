package my.trader.coin.dto.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TickerResponseDto {
  @JsonProperty("market")
  private String market;

  @JsonProperty("trade_date")
  private String tradeDate;

  @JsonProperty("trade_time")
  private String tradeTime;

  @JsonProperty("trade_date_kst")
  private String tradeDateKst;

  @JsonProperty("trade_time_kst")
  private String tradeTimeKst;

  @JsonProperty("trade_timestamp")
  private Long tradeTimestamp;

  @JsonProperty("opening_price")
  private Double openingPrice;

  @JsonProperty("high_price")
  private Double highPrice;

  @JsonProperty("low_price")
  private Double lowPrice;

  @JsonProperty("trade_price")
  private Double tradePrice;

  @JsonProperty("prev_closing_price")
  private Double prevClosingPrice;

  @JsonProperty("change")
  private String change;

  @JsonProperty("change_price")
  private Double changePrice;

  @JsonProperty("change_rate")
  private Double changeRate;

  @JsonProperty("signed_change_price")
  private Double signedChangePrice;

  @JsonProperty("signed_change_rate")
  private Double signedChangeRate;

  @JsonProperty("trade_volume")
  private Double tradeVolume;

  @JsonProperty("acc_trade_price")
  private Double accTradePrice;

  @JsonProperty("acc_trade_price_24h")
  private Double accTradePrice24h;

  @JsonProperty("acc_trade_volume")
  private Double accTradeVolume;

  @JsonProperty("acc_trade_volume_24h")
  private Double accTradeVolume24h;

  @JsonProperty("highest_52_week_price")
  private Double highest52WeekPrice;

  @JsonProperty("highest_52_week_date")
  private String highest52WeekDate;

  @JsonProperty("lowest_52_week_price")
  private Double lowest52WeekPrice;

  @JsonProperty("lowest_52_week_date")
  private String lowest52WeekDate;

  @JsonProperty("timestamp")
  private Long timestamp;
}
