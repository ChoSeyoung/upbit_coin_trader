package my.trader.coin.enums;

import lombok.Getter;

@Getter
public enum TradeType {
    BUY("BUY", "매수"),
    SELL("SELL", "매도");

    private final String name;
    private final String description;

    TradeType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
