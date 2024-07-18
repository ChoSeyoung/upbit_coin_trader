package my.trader.coin.enums;

import lombok.Getter;

/**
 * TradeType 은 내부적으로 매수/매도를 표현한 집합체입니다.
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
}
