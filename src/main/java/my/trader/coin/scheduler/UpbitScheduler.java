package my.trader.coin.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import my.trader.coin.enums.TickerSymbol;
import my.trader.coin.enums.TradeType;
import my.trader.coin.model.Trade;
import my.trader.coin.model.User;
import my.trader.coin.repository.TradeRepository;
import my.trader.coin.repository.UserRepository;
import my.trader.coin.service.UpbitService;
import my.trader.coin.strategy.ScalpingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * UpbitScheduler는 정기적으로 시장 데이터를 가져오고 트레이딩 전략을 실행합니다.
 */
@Component
public class UpbitScheduler {

    private static final Logger logger = LoggerFactory.getLogger(UpbitScheduler.class);

    private final UpbitService upbitService;
    private final ScalpingStrategy scalpingStrategy;
    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;

    DecimalFormat df = new DecimalFormat("#,##0.00"); // Format to two decimal places

    // 시뮬레이션 모드 플래그
    @Value("${simulation.mode}")
    private boolean simulationMode;

    // 티커 심볼
    private final String symbol = TickerSymbol.KRW_XRP.getSymbol();

    // 거래 수수료
    private final BigDecimal exchangeFeePercentage;

    public UpbitScheduler(UpbitService upbitService, ScalpingStrategy scalpingStrategy, TradeRepository tradeRepository, UserRepository userRepository, @Value("${upbit.exchange.fee}") String exchangeFeePercentage) {
        this.upbitService = upbitService;
        this.scalpingStrategy = scalpingStrategy;
        this.tradeRepository = tradeRepository;
        this.userRepository = userRepository;
        this.exchangeFeePercentage = new BigDecimal(exchangeFeePercentage);
    }

    /**
     * 매 분마다 시장 데이터를 가져오고 스캘핑 전략을 실행합니다.
     */
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void fetchMarketData() {
        try {
            // Upbit API를 통해 비트코인의 티커 데이터 가져오기
            JsonNode tickerData = upbitService.getTicker(TickerSymbol.KRW_BTC.getSymbol());
            BigDecimal currentPrice = tickerData.get(0).get("trade_price").decimalValue();
            BigDecimal currentVolume = tickerData.get(0).get("acc_trade_volume_24h").decimalValue(); // 예시로 24시간 거래량 사용
            System.out.println(LocalDateTime.now());
            System.out.println("Current Price: " + df.format(currentPrice));
            System.out.println("Current Volume: " + df.format(currentVolume));

            // 스캘핑 전략을 실행하여 매수 또는 매도 결정을 내림
            currentPrice = currentPrice.add(currentPrice.multiply(exchangeFeePercentage));
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
        Optional<User> userOptional = userRepository.findById(1L);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (scalpingStrategy.shouldBuy(currentPrice, currentVolume) && user.getInventory() == 0) {
                // 매수 신호가 발생하면 매수 로직 실행
                System.out.println("Simulated buying at price: " + df.format(currentPrice));
                saveUser("BUY", symbol, 0.01);
                saveTrade("BUY", symbol, currentPrice, 0.01, simulationMode);
                if (!simulationMode) {
                    // 실제 업비트 매수 API 호출 예시
                    boolean buySuccess = upbitService.executeBuyOrder(symbol, currentPrice, 0.01);
                    if (buySuccess) {
                        System.out.println("Successfully bought BTC at price: " + currentPrice);
                    } else {
                        System.out.println("Failed to buy BTC at price: " + currentPrice);
                    }
                }
            } else if (scalpingStrategy.shouldSell(currentPrice, currentVolume) && user.getInventory() > 0) {
                // 매도 신호가 발생하면 매도 로직 실행
                System.out.println("Simulated selling at price: " + df.format(currentPrice));
                saveUser("SELL", symbol, 0.01); // 사용자 인벤토리 업데이트
                saveTrade("SELL", symbol, currentPrice, 0.01, simulationMode); // 예제에서는 0.01 BTC 매도
                if (!simulationMode) {
                    // 실제 업비트 매도 API 호출 예시
                    boolean sellSuccess = upbitService.executeSellOrder("KRW-BTC", currentPrice, 0.01);
                    if (sellSuccess) {
                        System.out.println("Successfully sold BTC at price: " + currentPrice);
                    } else {
                        System.out.println("Failed to sell BTC at price: " + currentPrice);
                    }
                }
            }
        }
    }

    /**
     * 거래 정보를 저장하는 메서드
     *
     * @param type 거래 타입 (ex. BUY or SELL)
     * @param symbol 마켓 심볼 (ex. KRW-BTC)
     * @param price 거래 가격
     * @param quantity 거래 수량
     * @param simulationMode 모의투자 여부
     */
    private void saveTrade(String type, String symbol, BigDecimal price, double quantity, boolean simulationMode) {
        Trade trade = new Trade();
        trade.setSymbol(symbol);
        trade.setType(type); // 거래 타입 (매수 또는 매도)
        trade.setPrice(price); // 거래 가격
        trade.setQuantity(quantity); // 거래 수량
        trade.setTimestamp(LocalDateTime.now()); // 거래 시간
        trade.setSimulationMode(simulationMode);
        tradeRepository.save(trade); // 거래 정보를 저장소에 저장
    }

    /**
     * 사용자의 인벤토리를 업데이트하는 메서드
     *
     * @param type 거래 타입 (ex. BUY or SELL)
     * @param symbol 마켓 심볼 (ex. KRW-BTC)
     * @param quantity 거래 수량
     */
    private void saveUser(String type, String symbol, double quantity) {
        Optional<User> userOptional = userRepository.findById(1L);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            user.setSymbol(symbol);
            if (TradeType.BUY.getName().equalsIgnoreCase(type)) {
                user.setInventory(user.getInventory() + quantity);
            } else if (TradeType.SELL.getName().equalsIgnoreCase(type)) {
                user.setInventory(user.getInventory() - quantity);
            }

            userRepository.save(user);
        }
    }
}
