package my.trader.coin.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import my.trader.coin.enums.ColorfulConsoleOutput;
import my.trader.coin.enums.TickerSymbol;
import my.trader.coin.enums.TradeType;
import my.trader.coin.model.Trade;
import my.trader.coin.model.User;
import my.trader.coin.repository.TradeRepository;
import my.trader.coin.repository.UserRepository;
import my.trader.coin.service.UpbitService;
import my.trader.coin.strategy.ScalpingStrategy;
import my.trader.coin.util.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * UpbitScheduler 는 정기적으로 시장 데이터를 가져오고 트레이딩 전략을 실행합니다.
 */
@Component
public class UpbitScheduler {
  private static final Logger logger = LoggerFactory.getLogger(UpbitScheduler.class);

  // 거래수수료
  @Value("${upbit.ratio.exchange}")
  private double exchangeFeeRatio;
  // 시뮬레이션 모드 플래그
  @Value("${simulation.mode}")
  private boolean simulationMode;
  // 티커 심볼
  @Value("${upbit.ticker.symbol}")
  private String tickerSymbol;

  private final UpbitService upbitService;
  private final ScalpingStrategy scalpingStrategy;
  private final TradeRepository tradeRepository;
  private final UserRepository userRepository;

  // 콘솔 데이터 출력용 formatter
  DecimalFormat df = new DecimalFormat("#,##0.00");

  /**
   * this is constructor.
   *
   * @param upbitService     UpbitService
   * @param scalpingStrategy ScalpingStrategy
   * @param tradeRepository  TradeRepository
   * @param userRepository   UserRepository
   */
  public UpbitScheduler(
        UpbitService upbitService,
        ScalpingStrategy scalpingStrategy,
        TradeRepository tradeRepository,
        UserRepository userRepository
  ) {
    this.upbitService = upbitService;
    this.scalpingStrategy = scalpingStrategy;
    this.tradeRepository = tradeRepository;
    this.userRepository = userRepository;
  }

  /**
   * 매 분마다 시장 데이터를 가져오고 스캘핑 전략을 실행합니다.
   */
  @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
  public void fetchMarketData() {
    try {
      // 시장 데이터 조회
      JsonNode tickerData = upbitService.getTicker(tickerSymbol);
      // 현재 가격
      Double currentPrice = tickerData.get(0).get("trade_price").doubleValue();
      // 현재 거래량
      Double currentVolume = tickerData.get(0).get("acc_trade_volume_24h").doubleValue();
      // 현재 가격 및 거래량 로깅
      ColorfulConsoleOutput.printWithColor(
            String.format("Current Price & Volume: %s / %s",
                  df.format(currentPrice),
                  df.format(currentVolume)
            ),
            ColorfulConsoleOutput.YELLOW
      );

      // 스캘핑 전략을 실행하여 매수 또는 매도 결정을 내림
      executeScalpingStrategy(currentPrice, currentVolume);

      // 현재 수익률 조회 및 로깅
      calculateAndPrintProfit();
    } catch (Exception e) {
      // 예외 발생 시 로그에 에러 메시지 출력
      logger.error("시장 데이터를 가져오는 중 오류 발생", e);
    }
  }

  /**
   * 10초마다 매수/매도 주문이 정상적으로 실행되었는지 확인하고 업데이트 합니다.
   */
  @Scheduled(fixedRate = 10000) // 10초마다 실행
  public void checkTradeStatus() {
    // 시뮬레이션 모드에서는 실행하지 않습니다.
    if (!simulationMode) {
      try {
        // 거래완료 체크가 되지 않은 데이터 추출
        List<Trade> tradesToCheck = tradeRepository.findByIsSignedFalseOrIsSignedIsNull();

        // 거래완료 체크가 되지 않은 데이터가 없다면 스케줄러 종료
        if (tradesToCheck.isEmpty()) {
          return;
        }

        // 티커심볼을 기준으로 그룹화
        Map<String, List<Trade>> tradesByTicker = tradesToCheck.stream()
              .collect(Collectors.groupingBy(Trade::getTickerSymbol));

        // 각 티커심볼을 기준으로 업비트에 거래완료 여부 체크
        for (Map.Entry<String, List<Trade>> entry : tradesByTicker.entrySet()) {
          // 티커심볼
          String tickerSymbol = entry.getKey();
          // 티커심볼 기준 거래내역
          List<Trade> trades = entry.getValue();

          // 식별자 목록 생성
          List<String> identifiers = trades.stream()
                .map(Trade::getIdentifier)
                .collect(Collectors.toList());

          // 식별자를 사용하여 API 호출
          JsonNode response = upbitService.getOrderStatusByIds(tickerSymbol, identifiers);

          // 각 주문의 상태 확인
          for (JsonNode order : response) {
            // 식별자
            String identifier = order.get("uuid").asText();
            // 남은 체결량
            double remainingVolume = order.get("remaining_volume").asDouble();

            // 주문수량이 모두 체결되었을 경우 모두 체결된것으로 확인
            boolean isSigned = remainingVolume == 0;

            // 주문한 수량이 모두 체결되었다면 거래내역 업데이트
            if (isSigned) {
              Trade trade = tradeRepository.findByIdentifier(identifier);
              if (trade != null) {
                trade.setIsSigned(true);
                tradeRepository.save(trade);
              }
            }
          }
        }
      } catch (Exception e) {
        logger.error("주문 상태를 확인하는 중 오류 발생", e);
      }
    }
  }

  /**
   * 수익률 조회하여 콘솔에 로깅.
   */
  private void calculateAndPrintProfit() {
    try {
      // 매수 거래 내역 조회
      List<Trade> buyTrades = tradeRepository.findByType(TradeType.BUY.getName());
      // 판매 거래내역 조회
      List<Trade> sellTrades = tradeRepository.findByType(TradeType.SELL.getName());

      // 수익률을 모아줄 컬렉션
      List<Double> profitPercentages = new ArrayList<>();

      // 구매/판매 내역을 반복하면서 수익률 계산
      while (!buyTrades.isEmpty() && !sellTrades.isEmpty()) {
        // 가장 최근 구매내역과 가장 최근 판매내역을 매칭하여 수익률을 계산한다.
        Trade buyTrade = buyTrades.remove(0);
        Trade sellTrade = sellTrades.remove(0);

        // 수익금액
        double profit = sellTrade.getPrice() - buyTrade.getPrice();
        // 수익률
        double profitPercentage = (profit / buyTrade.getPrice()) * 100;
        // 수익률 저장
        profitPercentages.add(profitPercentage);
      }

      // 수익률 컬렉션이 비어있는 경우 거래내역이 없다는 메세지를 노출하고
      // 수익률 컬렉션이 비어있지 않은 경우엔 해당 컬렉션의 모든 수익률을 평균화하여 콘솔에 노출한다.
      if (profitPercentages.isEmpty()) {
        ColorfulConsoleOutput.printWithColor("No matched trades found.",
              ColorfulConsoleOutput.GREEN);
      } else {
        double averageProfitPercentage = profitPercentages.stream()
              .mapToDouble(Double::doubleValue)
              .average()
              .orElse(0.0);

        ColorfulConsoleOutput.printWithColor(
              String.format("Average Profit Percentage: %.2f%%", averageProfitPercentage),
              ColorfulConsoleOutput.GREEN);
      }
    } catch (Exception e) {
      // 예외 발생 시 로그에 에러 메시지 출력
      logger.error("거래 데이터를 가져오는 중 오류 발생", e);
    }
  }

  /**
   * 가져온 시장 데이터를 기반으로 스캘핑 전략을 실행합니다.
   *
   * @param currentPrice  자산의 현재 가격
   * @param currentVolume 자산의 현재 거래량
   */
  private void executeScalpingStrategy(double currentPrice, double currentVolume) {
    Optional<User> userOptional = userRepository.findById(1L);

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      double inventory = user.getInventory(tickerSymbol);

      if (scalpingStrategy.shouldBuy(currentPrice, currentVolume)) {
        // API 호출 식별자 생성
        String identifier =
              IdentifierGenerator.generateUniqueIdentifier(user.getId(), TradeType.BUY.getName());

        // 매수 신호가 발생하면 매수 로직 실행
        boolean buySuccess = upbitService.executeBuyOrder(tickerSymbol, currentPrice,
              TickerSymbol.getQuantityBySymbol(tickerSymbol), identifier, simulationMode);

        // 매수 주문 실행 성공 후 처리 프로세스
        if (buySuccess) {
          ColorfulConsoleOutput.printWithColor(
                String.format("Successfully bought %s at price: %s", tickerSymbol,
                      df.format(currentPrice)),
                ColorfulConsoleOutput.RED
          );

          // 계좌정보 업데이트
          saveUser(TradeType.BUY.getName(), tickerSymbol,
                TickerSymbol.getQuantityBySymbol(tickerSymbol));
          // 거래내역 업데이트
          saveTrade(TradeType.BUY.getName(), tickerSymbol, currentPrice,
                TickerSymbol.getQuantityBySymbol(tickerSymbol), identifier, simulationMode);
        }
      } else if (scalpingStrategy.shouldSell(currentPrice, currentVolume) && inventory > 0) {
        // API 호출 식별자 생성
        String identifier =
              IdentifierGenerator.generateUniqueIdentifier(user.getId(), TradeType.SELL.getName());

        // 매도 신호가 발생하면 매도 로직 실행
        boolean sellSuccess = upbitService.executeSellOrder(tickerSymbol, currentPrice,
              TickerSymbol.getQuantityBySymbol(tickerSymbol), identifier, simulationMode);

        // 매도 주문 실행 성공 후 처리 프로세스
        if (sellSuccess) {
          ColorfulConsoleOutput.printWithColor(
                String.format("Successfully sold %s at price: %s", tickerSymbol,
                      df.format(currentPrice)),
                ColorfulConsoleOutput.BLUE
          );

          // 계좌정보 업데이트
          saveUser(TradeType.SELL.getName(), tickerSymbol,
                TickerSymbol.getQuantityBySymbol(tickerSymbol));
          // 거래내역 업데이트
          saveTrade(TradeType.SELL.getName(), tickerSymbol, currentPrice,
                TickerSymbol.getQuantityBySymbol(tickerSymbol), identifier, simulationMode);
        }
      }
    }
  }

  /**
   * 거래 정보를 저장하는 메서드.
   *
   * @param type           거래 타입 (ex. BUY or SELL)
   * @param tickerSymbol   마켓 심볼 (ex. KRW-BTC)
   * @param price          거래 가격
   * @param quantity       거래 수량
   * @param identifier     식별자
   * @param simulationMode 모의투자 여부
   */
  private void saveTrade(String type, String tickerSymbol, double price, double quantity,
                         String identifier, boolean simulationMode) {
    Trade trade = new Trade();
    trade.setTickerSymbol(tickerSymbol);
    trade.setType(type); // 거래 타입 (매수 또는 매도)
    trade.setPrice(price); // 거래 가격
    trade.setQuantity(quantity); // 거래 수량
    trade.setExchangeFee(exchangeFeeRatio);
    trade.setTimestamp(LocalDateTime.now()); // 거래 시간
    trade.setIdentifier(identifier); // 식별자
    trade.setSimulationMode(simulationMode); // 시뮬레이션 모드
    trade.setIsSigned(false); // 체결여부
    tradeRepository.save(trade); // 거래 정보를 저장소에 저장
  }

  /**
   * 사용자의 인벤토리를 업데이트하는 메서드.
   *
   * @param type         거래 타입 (ex. BUY or SELL)
   * @param tickerSymbol 마켓 심볼 (ex. KRW-BTC)
   * @param quantity     거래 수량
   */
  private void saveUser(String type, String tickerSymbol, double quantity) {
    Optional<User> userOptional = userRepository.findById(1L);
    if (userOptional.isPresent()) {
      User user = userOptional.get();

      // 현재 심볼의 인벤토리 수량 가져오기
      double currentInventory = user.getInventory(tickerSymbol);

      // 거래 타입에 따라 인벤토리 업데이트
      if (TradeType.BUY.getName().equalsIgnoreCase(type)) {
        currentInventory += quantity;
      } else if (TradeType.SELL.getName().equalsIgnoreCase(type)) {
        currentInventory -= quantity;
      }

      // 업데이트된 인벤토리 수량 저장
      user.updateInventory(tickerSymbol, currentInventory);

      // 변경된 사용자 정보 저장
      userRepository.save(user);
    }
  }
}
