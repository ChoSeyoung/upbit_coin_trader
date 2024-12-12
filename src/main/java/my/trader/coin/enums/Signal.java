package my.trader.coin.enums;

/**
 * 매수, 매도(익절, 손절 포함), 액션에 대한 플래그.
 */
public enum Signal {
  NO_ACTION,
  BUY,
  TAKE_PROFIT,
  STOP_LOSS;

  public boolean isSellSignal() {
    return this == TAKE_PROFIT || this == STOP_LOSS;
  }

  public boolean isBuySignal() {
    return this == BUY;
  }
}