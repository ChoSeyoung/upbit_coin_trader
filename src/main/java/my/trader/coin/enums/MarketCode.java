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
  // 솔라나
  KRW_SOL("KRW-SOL"),
  // 도지코인
  KRW_DOGE("KRW-DOGE"),
  // 에이다
  KRW_ADA("KRW-ADA"),
  // 트론
  KRW_TRX("KRW-TRX"),
  // 아발란체
  KRW_AVAX("KRW-AVAX"),
  // 시바이누
  KRW_SHIB("KRW-SHIB"),
  // 스텔라루멘
  KRW_XML("KRW-XLM"),
  // 테더
  KRW_USDT("KRW-USDT"),
  // 매직에덴
  KRW_ME("KRW-ME"),
  // 크레딧코인
  KRW_CTC("KRW-CTC");


  private final String symbol;

  MarketCode(String symbol) {
    this.symbol = symbol;
  }
}
