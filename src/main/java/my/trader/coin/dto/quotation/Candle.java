package my.trader.coin.dto.quotation;

import lombok.Data;

@Data
public class Candle {
  private String market;
  private String candleDateTimeUtc;
  private String candleDateTimeKst;
  private Double openingPrice;
  private Double highPrice;
  private Double lowPrice;
  private Double tradePrice;
  private Long timestamp;
  private Double candleAccTradePrice;
  private Double candleAccTradeVolume;
  private Integer unit;
}
