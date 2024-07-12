package my.trader.coin.enums;

import lombok.Getter;

/**
 * TickerSymbol enum은 Upbit에서 사용할 수 있는 다양한 거래 쌍을 나타냅니다.
 * 각 enum 상수는 특정 거래 쌍에 대응합니다.
 */
@Getter
public enum TickerSymbol {
    KRW_BTC("KRW-BTC"),  // 비트코인/한국 원화
    KRW_ETH("KRW-ETH"),  // 이더리움/한국 원화
    KRW_XRP("KRW-XRP"),  // 리플/한국 원화
    KRW_ADA("KRW-ADA"),  // 에이다/한국 원화
    KRW_DOGE("KRW-DOGE"),  // 도지코인/한국 원화
    KRW_SOL("KRW-SOL"),  // 솔라나/한국 원화
    KRW_DOT("KRW-DOT"),  // 폴카닷/한국 원화
    KRW_AVAX("KRW-AVAX"),  // 아발란체/한국 원화
    KRW_TRX("KRW-TRX"),  // 트론/한국 원화
    KRW_ATOM("KRW-ATOM"),  // 코스모스/한국 원화
    KRW_LINK("KRW-LINK"),  // 체인링크/한국 원화
    KRW_BCH("KRW-BCH"),  // 비트코인캐시/한국 원화
    KRW_LTC("KRW-LTC"),  // 라이트코인/한국 원화
    KRW_MANA("KRW-MANA"),  // 디센트럴랜드/한국 원화
    KRW_SAND("KRW-SAND"),  // 샌드박스/한국 원화
    KRW_EOS("KRW-EOS"),  // 이오스/한국 원화
    KRW_BAT("KRW-BAT"),  // 베이직어텐션토큰/한국 원화
    KRW_AAVE("KRW-AAVE"),  // 아베/한국 원화
    KRW_ZIL("KRW-ZIL"),  // 질리카/한국 원화
    KRW_XLM("KRW-XLM");  // 스텔라루멘/한국 원화

    private final String symbol;

    TickerSymbol(String symbol) {
        this.symbol = symbol;
    }

}
