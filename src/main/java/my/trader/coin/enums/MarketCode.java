package my.trader.coin.enums;

import lombok.Getter;

/**
 * market 은 Upbit 에서 사용할 수 있는 다양한 Symbol 의 집합체입니다.
 * 해당 심볼은 원화를 기준으로 작성되었습니다.
 * 필요할 경우 추가하도록 합니다.
 */
@Getter
public enum MarketCode {
  // 비트코인
  KRW_BTC("KRW-BTC"),
  // 이더리움
  KRW_ETH("KRW-ETH"),
  // 리플
  KRW_XRP("KRW-XRP");

  private final String symbol;

  MarketCode(String symbol) {
    this.symbol = symbol;
  }
}
