package my.trader.coin.enums;

import lombok.Getter;

/**
 * market 은 Upbit 에서 사용할 수 있는 다양한 Symbol 의 집합체입니다.
 * 해당 심볼은 원화를 기준으로 작성되었습니다.
 * 필요할 경우 추가하도록 합니다.
 */
@Getter
public enum MarketCode {
  // 비트코인
  KRW_BTC("KRW-BTC", 1),
  // 이더리움
  KRW_ETH("KRW-ETH", 1),
  // 리플
  KRW_XRP("KRW-XRP", 1);

  private final String symbol;
  private final double quantity;

  MarketCode(String symbol, double quantity) {
    this.symbol = symbol;
    this.quantity = quantity;
  }

  /**
   * symbol 을 기준으로 quantity 를 반환하는 메서드.
   *
   * @param symbol symbol
   * @return quantity
   */
  public static double getQuantityBySymbol(String symbol) {
    for (MarketCode marketCode : values()) {
      if (marketCode.getSymbol().equals(symbol)) {
        return marketCode.getQuantity();
      }
    }
    throw new IllegalArgumentException("Invalid Ticker Symbol: " + symbol);
  }
}
