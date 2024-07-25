package my.trader.coin.dto.quotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CandleResponseDto {

  @JsonProperty("market")
  private String market;

  @JsonProperty("candle_date_time_utc")
  private String candleDateTimeUtc;

  @JsonProperty("candle_date_time_kst")
  private String candleDateTimeKst;

  @JsonProperty("opening_price")
  private Double openingPrice;

  @JsonProperty("high_price")
  private Double highPrice;

  @JsonProperty("low_price")
  private Double lowPrice;

  @JsonProperty("trade_price")
  private Double tradePrice;

  @JsonProperty("timestamp")
  private Long timestamp;

  @JsonProperty("candle_acc_trade_price")
  private Double candleAccTradePrice;

  @JsonProperty("candle_acc_trade_volume")
  private Double candleAccTradeVolume;

  @JsonProperty("unit")
  private Integer unit;
}
