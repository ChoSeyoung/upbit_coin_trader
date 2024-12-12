package my.trader.coin.enums;

import lombok.Getter;

/**
 * 업비트에서 사용되는 API 에 대한 목록을 나열합니다.
 */
@Getter
public enum UpbitApi {
  // UBMI 10 인덱스 조회 (비공식)
  GET_UPBIT_MARKET_INDEX_TOP10("https://ubci-api.ubcindex.com/v1/crix/index/recents?codes=IDX.UPBIT.UTTI"),
  GET_MARKET("https://api.upbit.com/v1/market/all"),
  GET_ACCOUNT("https://api.upbit.com/v1/accounts"),
  GET_TICKER("https://api.upbit.com/v1/ticker"),
  POST_ORDER("https://api.upbit.com/v1/orders"),
  GET_MINUTE_CANDLE("https://api.upbit.com/v1/candles/minutes/%s"),
  GET_OPEN_ORDER("https://api.upbit.com/v1/orders/open"),
  GET_CLOSED_ORDER("https://api.upbit.com/v1/orders/closed"),
  DELETE_CANCEL_ORDER("https://api.upbit.com/v1/order");

  private final String url;

  UpbitApi(String url) {
    this.url = url;
  }
}

