package my.trader.coin.enums;

import lombok.Getter;

@Getter
public enum UpbitType {
  // 매수
  ORDER_SIDE_BID("bid"),
  // 매도
  ORDER_SIDE_ASK("ask"),
  // 지정가
  ORDER_TYPE_LIMIT("limit"),
  // 시장가주문(매수)
  ORDER_TYPE_PRICE("price"),
  // 시장가주문(매도)
  ORDER_TYPE_MARKET("market"),
  // 최유리주문
  ORDER_TYPE_BEST("best"),
  // 캔들 개수(1~200개까지 요청 가능)
  MIN_CANDLE_SIZE("1"),
  MAX_CANDLE_SIZE("200");

  private final String type;

  UpbitType(String type) {
    this.type = type;
  }
}
