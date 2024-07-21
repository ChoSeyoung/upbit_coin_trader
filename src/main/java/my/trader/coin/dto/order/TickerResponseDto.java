package my.trader.coin.dto.order;

import lombok.Data;

@Data
public class TickerResponseDto {
  private String market;
  private String tradeDate;
  private String tradeTime;
  private String tradeDateKst;
  private String tradeTimeKst;
  private Long tradeTimestamp;
  private Double openingPrice;
  private Double highPrice;
  private Double lowPrice;
  private Double tradePrice;
  private Double prevClosingPrice;
  private String change;
  private Double changePrice;
  private Double changeRate;
  private Double signedChangePrice;
  private Double signedChangeRate;
  private Double tradeVolume;
  private Double accTradePrice;
  private Double accTradePrice24h;
  private Double accTradeVolume;
  private Double accTradeVolume24h;
  private Double highest52WeekPrice;
  private String highest52WeekDate;
  private Double lowest52WeekPrice;
  private String lowest52WeekDate;
  private Long timestamp;
}
