package my.trader.coin.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import my.trader.coin.model.Trade;
import my.trader.coin.repository.TradeRepository;
import my.trader.coin.service.UpbitService;
import my.trader.coin.strategy.ScalpingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UpbitScheduler {

    private static final Logger logger = LoggerFactory.getLogger(UpbitScheduler.class);

    private final UpbitService upbitService;
    private final ScalpingStrategy scalpingStrategy;
    private final TradeRepository tradeRepository;

    public UpbitScheduler(UpbitService upbitService, ScalpingStrategy scalpingStrategy, TradeRepository tradeRepository) {
        this.upbitService = upbitService;
        this.scalpingStrategy = scalpingStrategy;
        this.tradeRepository = tradeRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void fetchMarketData() {
        try {
            JsonNode tickerData = upbitService.getTicker("KRW-BTC");
            double currentPrice = tickerData.get(0).get("trade_price").asDouble();
            System.out.println("Current Price: " + currentPrice);

            executeScalpingStrategy(currentPrice);
        } catch (Exception e) {
            logger.error("Error fetching market data", e);
        }
    }

    private void executeScalpingStrategy(double currentPrice) {
        if (scalpingStrategy.shouldBuy(currentPrice)) {
            // 매수 로직을 여기에 추가합니다.
            System.out.println("Buying at price: " + currentPrice);
            saveTrade("BUY", currentPrice, 0.01); // 예제에서는 0.01 BTC 매수
        } else if (scalpingStrategy.shouldSell(currentPrice)) {
            // 매도 로직을 여기에 추가합니다.
            System.out.println("Selling at price: " + currentPrice);
            saveTrade("SELL", currentPrice, 0.01); // 예제에서는 0.01 BTC 매도
        }
    }

    private void saveTrade(String type, double price, double quantity) {
        Trade trade = new Trade();
        trade.setType(type);
        trade.setPrice(price);
        trade.setQuantity(quantity);
        trade.setTimestamp(LocalDateTime.now());
        tradeRepository.save(trade);
    }
}

