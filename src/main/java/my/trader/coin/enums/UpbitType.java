package my.trader.coin.enums;

import lombok.Getter;

/**
 * TickerSymbol 은 Upbit 에서 사용할 수 있는 다양한 API 의 집합체입니다.
 */
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
  ORDER_TYPE_BEST("best");

  private final String type;

  UpbitType(String type) {
    this.type = type;
  }
}
