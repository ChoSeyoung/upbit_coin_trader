package my.trader.coin.enums;

import lombok.Getter;

/**
 * 업비트 정책
 * 원화 기준 종목 코드.
 */
@Getter
public enum MarketCode {
  // 비트코인
  KRW_BTC("KRW-BTC"),
  // 이더리움
  KRW_ETH("KRW-ETH"),
  // 리플
  KRW_XRP("KRW-XRP"),
  // USDT
  KRW_USDT("KRW-USDT");

  private final String symbol;

  MarketCode(String symbol) {
    this.symbol = symbol;
  }
}
