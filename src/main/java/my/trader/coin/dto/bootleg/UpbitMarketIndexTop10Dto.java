package my.trader.coin.dto.bootleg;

import lombok.Data;

@Data
public class UpbitMarketIndexTop10Dto {
  // ex: IDX.UPBIT.UTTI
  private String code;
  // ex: 2024-12-12T13:49:49+00:00
  private String dateTime;
  // ex: 18303.82797259,
  private double openingPrice;
  // ex: 18581.17861445,
  private double highPrice;
  // ex: 18178.45938271,
  private double lowPrice;
  // ex: 18463.24871477,
  private double tradePrice;
  // ex: 18286.70530533,
  private double prevClosingPrice;
  // ex: RISE
  private String change;
  // ex: 176.54340944,
  private double changePrice;
  // ex: 1734011389000,
  private long timestamp;
  // ex: 176.54340944,
  private double signedChangePrice;
  // ex: 0.00965419,
  private double changeRate;
  // ex: 0.00965419
  private double signedChangeRate;
}
