package my.trader.coin.enums;

import lombok.Getter;

@Getter
public enum UpbitApi {
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

