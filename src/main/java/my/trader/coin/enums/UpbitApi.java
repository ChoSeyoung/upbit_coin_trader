package my.trader.coin.enums;

import lombok.Getter;

/**
 * TickerSymbol 은 Upbit 에서 사용할 수 있는 다양한 API 의 집합체입니다.
 */
@Getter
public enum UpbitApi {
  // 전체계좌조회
  RESET("/v1/accounts"),
  // 주문하기
  ORDERS("v1/orders");

  private final String url;

  UpbitApi(String url) {
    this.url = String.format("https://api.upbit.com%s", url);
  }
}

