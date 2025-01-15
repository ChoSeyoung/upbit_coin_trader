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
  KRW_CTC("KRW-CTC"),
  // 비트코인골드
  KRW_BTG("KRW-BTG"),
  // 에이브
  KRW_AAVE("KRW-AAVE"),
  // 수이
  KRW_SUI("KRW-SUI"),
  // 온도파이낸스
  KRW_ONDO("KRW-ONDO"),
  // 페페
  KRW_PEPE("KRW-PEPE"),
  // 샌드박스
  KRW_SAND("KRW-SAND"),
  // 체인링크
  KRW_LINK("KRW-LINK"),
  // 헤데라
  KRW_HBAR("KRW-HBAR"),
  // 아이오타
  KRW_IOTA("KRW-IOTA");


  private final String symbol;

  MarketCode(String symbol) {
    this.symbol = symbol;
  }
}
