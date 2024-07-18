package my.trader.coin.enums;

import lombok.Getter;

/**
 * 콘솔에 출력할 때 텍스트의 색상을 변경하는 데 사용할 ANSI 컬러 코드 집합체입니다.
 */
@Getter
public enum UpbitAPI {
  // 전체계좌조회
  RESET("/v1/accounts"),
  // 주문하기
  ORDERS("v1/orders");

  private final String url;

  UpbitAPI(String url) {
    this.url = String.format("https://api.upbit.com%s", url);
  }
}

