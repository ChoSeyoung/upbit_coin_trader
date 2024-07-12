package my.trader.coin.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import my.trader.coin.enums.TickerSymbol;
import my.trader.coin.model.Trade;
import my.trader.coin.repository.TradeRepository;
import my.trader.coin.service.UpbitService;
import my.trader.coin.strategy.ScalpingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

/**
 * UpbitScheduler는 정기적으로 시장 데이터를 가져오고 트레이딩 전략을 실행합니다.
 */
@Component
public class UpbitScheduler {

    private static final Logger logger = LoggerFactory.getLogger(UpbitScheduler.class);

    private final UpbitService upbitService;
    private final ScalpingStrategy scalpingStrategy;
    private final TradeRepository tradeRepository;

    DecimalFormat df = new DecimalFormat("#,##0.00"); // Format to two decimal places

    // 시뮬레이션 모드 플래그
    private final boolean simulationMode = true;

    public UpbitScheduler(UpbitService upbitService, ScalpingStrategy scalpingStrategy, TradeRepository tradeRepository) {
        this.upbitService = upbitService;
        this.scalpingStrategy = scalpingStrategy;
        this.tradeRepository = tradeRepository;
    }

    /**
     * 매 분마다 시장 데이터를 가져오고 스캘핑 전략을 실행합니다.
     */
    @Scheduled(fixedRate = 60000) // 1분 간격으로 실행
    public void fetchMarketData() {
        try {
            // Upbit API를 통해 비트코인의 티커 데이터 가져오기
            JsonNode tickerData = upbitService.getTicker(TickerSymbol.KRW_BTC);
            BigDecimal currentPrice = tickerData.get(0).get("trade_price").decimalValue();
            BigDecimal currentVolume = tickerData.get(0).get("acc_trade_volume_24h").decimalValue(); // 예시로 24시간 거래량 사용
            System.out.println("Current Price: " + df.format(currentPrice));
            System.out.println("Current Volume: " + df.format(currentVolume));

            // 스캘핑 전략을 실행하여 매수 또는 매도 결정을 내림
            executeScalpingStrategy(currentPrice, currentVolume);
        } catch (Exception e) {
            // 예외 발생 시 로그에 에러 메시지 출력
            logger.error("시장 데이터를 가져오는 중 오류 발생", e);
        }
    }

    /**
     * 가져온 시장 데이터를 기반으로 스캘핑 전략을 실행합니다.
     *
     * @param currentPrice 자산의 현재 가격
     * @param currentVolume 자산의 현재 거래량
     */
    private void executeScalpingStrategy(BigDecimal currentPrice, BigDecimal currentVolume) {
        if (scalpingStrategy.shouldBuy(currentPrice, currentVolume)) {
            // 매수 신호가 발생하면 매수 로직 실행
            System.out.println("Simulated buying at price: " + currentPrice);
            if (!simulationMode) {
                saveTrade("BUY", currentPrice, 0.01); // 예제에서는 0.01 BTC 매수
            }
        } else if (scalpingStrategy.shouldSell(currentPrice, currentVolume)) {
            // 매도 신호가 발생하면 매도 로직 실행
            System.out.println("Simulated selling at price: " + currentPrice);
            if (!simulationMode) {
                saveTrade("SELL", currentPrice, 0.01); // 예제에서는 0.01 BTC 매도
            }
        }
    }

    /**
     * 거래 정보를 저장하는 메서드
     *
     * @param type 거래 타입 (BUY 또는 SELL)
     * @param price 거래 가격
     * @param quantity 거래 수량
     */
    private void saveTrade(String type, BigDecimal price, double quantity) {
        Trade trade = new Trade();
        trade.setType(type); // 거래 타입 (매수 또는 매도)
        trade.setPrice(price); // 거래 가격
        trade.setQuantity(quantity); // 거래 수량
        trade.setTimestamp(LocalDateTime.now()); // 거래 시간
        tradeRepository.save(trade); // 거래 정보를 저장소에 저장
    }
}
