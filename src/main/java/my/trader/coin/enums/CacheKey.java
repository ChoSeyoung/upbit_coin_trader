package my.trader.coin.enums;

import lombok.Getter;

@Getter
public enum CacheKey {
  WHOLE_SELL_WHEN_PROFIT("whole_sell_when_profit"),
  SCHEDULED_MARKET("scheduled_market");

  private final String key;

  CacheKey(String key) {
    this.key = key;
  }
}
