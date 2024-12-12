package my.trader.coin.enums;

import lombok.Getter;

/**
 * 업비트 정책
 * 업비트에서 사용되는 타입에 대한 정의.
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
  ORDER_TYPE_BEST("best"),
  // 캔들 개수(1~200개까지 요청 가능)
  MIN_CANDLE_SIZE("1"),
  MAX_CANDLE_SIZE("200"),
  // EVEN : 보합
  TICKER_CHANGE_EVEN("EVEN"),
  // RISE : 상승
  TICKER_CHANGE_RISE("RISE"),
  // FALL : 하락
  TICKER_CHANGE_FALL("FALL");


  private final String type;

  UpbitType(String type) {
    this.type = type;
  }
}
