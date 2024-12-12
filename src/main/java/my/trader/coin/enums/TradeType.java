package my.trader.coin.enums;

import lombok.Getter;

/**
 * 업비트 정책
 * 매수/매도 타입.
 */
@Getter
public enum TradeType {
  BUY("BUY", "매수"),
  SELL("SELL", "매도");

  private final String name;
  private final String description;

  TradeType(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public boolean isBuy() {
    return this == BUY;
  }

  public boolean isSell() {
    return this == SELL;
  }
}
