package my.trader.coin.enums;

import lombok.Getter;

/**
 * TickerSymbol 은 Upbit 에서 사용할 수 있는 다양한 Symbol 의 집합체입니다.
 * 해당 심볼은 원화를 기준으로 작성되었습니다.
 * 필요할 경우 추가하도록 합니다.
 */
@Getter
public enum TickerSymbol {
  KRW_BTC("KRW-BTC"),  // 비트코인
  KRW_ETH("KRW-ETH"),  // 이더리움
  KRW_XRP("KRW-XRP"),  // 리플
  KRW_ADA("KRW-ADA"),  // 에이다
  KRW_DOGE("KRW-DOGE"),  // 도지코인
  KRW_SOL("KRW-SOL"),  // 솔라나
  KRW_DOT("KRW-DOT"),  // 폴카닷
  KRW_AVAX("KRW-AVAX"),  // 아발란체
  KRW_TRX("KRW-TRX"),  // 트론
  KRW_ATOM("KRW-ATOM"),  // 코스모스
  KRW_LINK("KRW-LINK"),  // 체인링크
  KRW_BCH("KRW-BCH"),  // 비트코인캐시
  KRW_LTC("KRW-LTC"),  // 라이트코인
  KRW_MANA("KRW-MANA"),  // 디센트럴랜드
  KRW_SAND("KRW-SAND"),  // 샌드박스
  KRW_EOS("KRW-EOS"),  // 이오스
  KRW_BAT("KRW-BAT"),  // 베이직어텐션토큰
  KRW_AAVE("KRW-AAVE"),  // 아베
  KRW_ZIL("KRW-ZIL"),  // 질리카
  KRW_XLM("KRW-XLM");  // 스텔라루멘

  private final String symbol;

  TickerSymbol(String symbol) {
    this.symbol = symbol;
  }
}
