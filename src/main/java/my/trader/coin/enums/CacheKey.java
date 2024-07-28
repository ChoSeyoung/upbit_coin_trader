package my.trader.coin.enums;

import lombok.Getter;

@Getter
public enum CacheKey {
  WHOLE_SELL_WHEN_PROFIT("whole_sell_when_profit"),
  SCHEDULED_MARKET("scheduled_market"),
  EXCHANGE_FEE_RATIO("exchange_fee_ratio"),
  TAKE_PROFIT_PERCENTAGE("take_profit_percentage"),
  MIN_ORDER_AMOUNT("min_order_amount");

  private final String key;

  CacheKey(String key) {
    this.key = key;
  }
}
