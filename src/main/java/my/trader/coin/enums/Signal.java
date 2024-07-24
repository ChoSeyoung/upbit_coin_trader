package my.trader.coin.enums;

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