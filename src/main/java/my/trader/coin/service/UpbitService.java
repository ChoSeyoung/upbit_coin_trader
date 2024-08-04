package my.trader.coin.service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import my.trader.coin.dto.exchange.*;
import my.trader.coin.dto.quotation.*;
import my.trader.coin.enums.ColorfulConsoleOutput;
import my.trader.coin.enums.Unit;
import my.trader.coin.enums.UpbitApi;
import my.trader.coin.enums.UpbitType;
import my.trader.coin.model.Config;
import my.trader.coin.repository.ClosedOrderRepository;
import my.trader.coin.util.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UpbitService {
  private final AuthorizationGenerator authorizationGenerator;
  private final ExternalUtility externalUtility;
  private final ClosedOrderService closedOrderService;
  private final ConfigService configService;
  private final ClosedOrderRepository closedOrderRepository;

  public UpbitService(AuthorizationGenerator authorizationGenerator,
                      ExternalUtility externalUtility,
                      ClosedOrderService closedOrderService, ConfigService configService,
                      ClosedOrderRepository closedOrderRepository) {
    this.authorizationGenerator = authorizationGenerator;
    this.externalUtility = externalUtility;
    this.closedOrderService = closedOrderService;
    this.configService = configService;
    this.closedOrderRepository = closedOrderRepository;
  }

  public List<MarketResponseDto> getMarket() {
    MarketRequestDto marketRequestDto = MarketRequestDto.builder().isDetail(true).build();

    String url = UpbitApi.GET_MARKET.getUrl();
    String parameters = CharacterUtility.createQueryString(marketRequestDto, false);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    return externalUtility.getWithoutAuth(uri, MarketResponseDto.class);
  }

  public List<AccountResponseDto> getAccount() {
    URI uri = UriComponentsBuilder.fromHttpUrl(UpbitApi.GET_ACCOUNT.getUrl()).build().toUri();

    String authorizationToken = authorizationGenerator.generateTokenWithoutParameter();

    return externalUtility.getWithAuth(uri, AccountResponseDto.class, authorizationToken);
  }

  public List<TickerResponseDto> getTicker(List<String> markets) {
    TickerRequestDto tickerRequestDto = TickerRequestDto.builder()
          .markets(String.join(",", markets))
          .build();

    String url = UpbitApi.GET_TICKER.getUrl();
    String parameters = CharacterUtility.createQueryString(tickerRequestDto, false);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    return externalUtility.getWithoutAuth(uri, TickerResponseDto.class);
  }

  public OrderResponseDto executeOrder(String tickerSymbol, double price, double quantity,
                                       String side) {
    OrderRequestDto orderRequestDto = OrderRequestDto.builder()
          .market(tickerSymbol)
          .side(side)
          .volume(quantity)
          .price(price)
          .ordType(UpbitType.ORDER_TYPE_LIMIT.getType())
          .build();

    String url = UpbitApi.POST_ORDER.getUrl();
    String parameters = CharacterUtility.createQueryString(orderRequestDto, false);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    String authorizationToken = authorizationGenerator.generateTokenWithParameter(orderRequestDto);

    return externalUtility.postWithAuth(uri, orderRequestDto, OrderResponseDto.class,
          authorizationToken);
  }

  public List<CandleResponseDto> getMinuteCandle(String market, Unit unit, int count,
                                                 String orderBy) {
    int minCandleSize = Integer.parseInt(UpbitType.MIN_CANDLE_SIZE.getType());
    int maxCandleSize = Integer.parseInt(UpbitType.MAX_CANDLE_SIZE.getType());

    if (count < minCandleSize || count > maxCandleSize) {
      System.err.printf("count %d out of %d ~ %d%n", count, minCandleSize, maxCandleSize);
    }

    CandleRequestDto candleRequestDto = CandleRequestDto.builder()
          .market(market)
          .count(count)
          .build();

    // unit 파라미터에 따라
    String url = String.format(UpbitApi.GET_MINUTE_CANDLE.getUrl(), unit.getUnit());
    String parameters = CharacterUtility.createQueryString(candleRequestDto, false);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    List<CandleResponseDto> candleResponseDtos =
          externalUtility.getWithoutAuth(uri, CandleResponseDto.class);

    // orderBy 인자에 따라 정렬
    if (orderBy.equalsIgnoreCase("asc")) {
      candleResponseDtos.sort(Comparator.comparingLong(CandleResponseDto::getTimestamp));
    }

    return candleResponseDtos;
  }

  public List<OpenOrderResponseDto> getOpenOrders(String market) {
    OpenOrderRequestDto openOrderRequestDto = OpenOrderRequestDto.builder()
          .market(market).build();

    String url = UpbitApi.GET_OPEN_ORDER.getUrl();

    String parameters = CharacterUtility.createQueryString(openOrderRequestDto, false);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    String authorizationToken =
          authorizationGenerator.generateTokenWithParameter(openOrderRequestDto);

    return externalUtility.getWithAuth(uri, OpenOrderResponseDto.class, authorizationToken);
  }

  public CancelOrderResponseDto cancelOrder(String uuid) {
    CancelOrderRequestDto cancelOrderRequestDto = CancelOrderRequestDto.builder()
          .uuid(uuid)
          .build();

    String url = UpbitApi.DELETE_CANCEL_ORDER.getUrl();
    String parameters = CharacterUtility.createQueryString(cancelOrderRequestDto, false);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    String authorizationToken =
          authorizationGenerator.generateTokenWithParameter(cancelOrderRequestDto);

    return externalUtility.deleteWithAuth(uri, cancelOrderRequestDto,
          CancelOrderResponseDto.class,
          authorizationToken);
  }

  public List<CancelOrderResponseDto> beforeTaskExecution() {
    List<CancelOrderResponseDto> results = new ArrayList<>();

    // 계좌 조회
    List<AccountResponseDto> accounts = this.getAccount();

    // 종목별 미체결 주문 조회
    for (AccountResponseDto accountResponseDto : accounts) {
      // 현금 주문은 조회 제외
      if (accountResponseDto.getCurrency().equals("KRW")) {
        continue;
      }

      // 마켓코드 할당
      String market = String.format("KRW-%s", accountResponseDto.getCurrency());

      // 미체결 주문 조회
      List<OpenOrderResponseDto> openOrders = this.getOpenOrders(market);

      // 각 uuid 기준으로 주문 취소 요청
      for (OpenOrderResponseDto openOrderResponseDto : openOrders) {
        // uuid 조회
        String uuid = openOrderResponseDto.getUuid();

        // 미체결 주문 취소 요청
        CancelOrderResponseDto result = this.cancelOrder(uuid);

        // 결과값 추가
        results.add(result);
      }
    }

    return results;
  }

  public List<CancelOrderResponseDto> afterTaskCompletion() {
    List<CancelOrderResponseDto> results = new ArrayList<>();

    // 계좌 조회
    List<AccountResponseDto> accounts = this.getAccount();

    // 종목별 미체결 주문 조회
    for (AccountResponseDto accountResponseDto : accounts) {
      // 현금 주문은 조회 제외
      if (accountResponseDto.getCurrency().equals("KRW")) {
        continue;
      }

      // 마켓코드 할당
      String market = String.format("KRW-%s", accountResponseDto.getCurrency());

      // 미체결 주문 조회
      List<OpenOrderResponseDto> openOrders = this.getOpenOrders(market);

      // 각 uuid 기준으로 주문 취소 요청
      for (OpenOrderResponseDto openOrderResponseDto : openOrders) {
        // 매도 주문은 해당 메서드에서 처리하지 않음 (#23)
        if (openOrderResponseDto.getSide().equals("ask")) continue;

        // uuid 조회
        String uuid = openOrderResponseDto.getUuid();

        // 미체결 주문 취소 요청
        CancelOrderResponseDto result = this.cancelOrder(uuid);

        // 결과값 추가
        results.add(result);
      }
    }

    return results;
  }

  public List<ClosedOrderResponseDto> initializeClosedOrders(String type) {
    OffsetDateTime startTime;

    if ("scheduler".equals(type)) {
      // 스케줄러를 통한 경우, closed_order 테이블에서 created_at 이 가장 마지막 데이터를 기준으로 1초 추가 되어야한다.
      OffsetDateTime lastCreatedAt = closedOrderRepository.findLastCreatedAt();
      if (lastCreatedAt == null) {
        // DB에 데이터가 없는 경우 현재 날짜로 세팅
        startTime = OffsetDateTime.now(ZoneOffset.ofHours(9));
      } else {
        startTime = lastCreatedAt.plusSeconds(1);
      }
    } else {
      // 초기화 요청인 경우 2024-6-16 22:00:00 기준으로 시작되어 현재시간까지 반복되어야한다.
      startTime = OffsetDateTime.of(2024, 7, 26, 0, 0, 0, 0, ZoneOffset.ofHours(9));
      // closed_orders 테이블 초기화
      closedOrderService.initClosedOrders();
    }

    // formatter 설정
    DateTimeFormatter timestampFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    // 전체 마감 데이터
    List<ClosedOrderResponseDto> allClosedOrders = new ArrayList<>();
    // while 문 종료 시간 설정
    OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Seoul"));
    // 종료시간 까지 반복
    while (startTime.isBefore(now)) {
      ClosedOrderRequestDto closedOrderRequestDto = ClosedOrderRequestDto.builder()
            .startTime(startTime.format(timestampFormatter))
            .state("done")
            .limit("1000")
            .orderBy("asc")
            .build();

      String url = UpbitApi.GET_CLOSED_ORDER.getUrl();
      String parameters = CharacterUtility.createQueryString(closedOrderRequestDto, true);

      URI uri = URI.create(url + "?" + parameters);

      String authorizationToken = authorizationGenerator.generateTokenWithParameter(
            closedOrderRequestDto);

      List<ClosedOrderResponseDto> response =
            externalUtility.getWithAuth(uri, ClosedOrderResponseDto.class, authorizationToken);
      if (response != null) {
        allClosedOrders.addAll(response);
        closedOrderService.saveClosedOrder(allClosedOrders);
      }

      // Increment the time range by 1 hour
      startTime = startTime.plusHours(1); // 1초 추가하여 다음 시작 시간 설정
      TimeUtility.sleep(1);
    }

    return allClosedOrders;
  }

  /**
   * 지수 이동 평균 데이터를 기준으로 RSI 지표 조회.
   *
   * @param market 마켓코드
   * @param weight 가중치
   * @return RSI
   */
  public Double calculateRelativeStrengthIndex(String market, int weight) {
    int maxCandleSize = Integer.parseInt(UpbitType.MAX_CANDLE_SIZE.getType());

    // timestamp 기준 오름차순 정렬된 지수 이동 평균 데이터 조회
    List<CandleResponseDto> candles =
          getMinuteCandle(market, Unit.UNIT_1, maxCandleSize, "asc");
    // 마지막 데이터는 현재 분에 해당하는 캔들이므로 제거
    candles.remove(candles.size() - 1);

    // 상승 데이터
    List<Double> up = new ArrayList<>();
    // 하락 데이터
    List<Double> down = new ArrayList<>();

    for (int i = 0; i < candles.size() - 1; i++) {
      // gap = 최근 종가 - 전일 종가
      // gap 양수 = 상승 / gap 음수 = 하락
      double gap = candles.get(i + 1).getTradePrice() - candles.get(i).getTradePrice();

      if (gap > 0) {
        // 종가가 전일 종가보다 상승일 경우
        up.add(gap);
        down.add(0.0);
      } else if (gap < 0) {
        // 종가가 전일 종가보다 하락일 경우
        down.add(gap * -1);
        up.add(0.0);
      } else {
        // 상승, 하락이 없을 경우
        up.add(0.0);
        down.add(0.0);
      }
    }

    // AU 계산
    double au = MathUtility.calculateExponentialMovingAverage(up, weight);
    // AD 계산
    double ad = MathUtility.calculateExponentialMovingAverage(down, weight);

    return 100 - (100 / (1 + (au / ad)));
  }

  public void selectScheduledMarket() {
    List<MarketResponseDto> marketResponses = getMarket();

    List<String> markets = marketResponses.stream()
          .map(MarketResponseDto::getMarket)
          .toList();

    List<TickerResponseDto> tickers = getTicker(markets);

    // 현재 상승중인 종목 & 24시간 누적 거래액이 1000억 이상 & 변화율이 5% 이상 => 변화율 기준 내림차순 정렬
    List<TickerResponseDto> analyzedTickers = tickers.stream()
          .filter(ticker -> ticker.getChange().equals(UpbitType.TICKER_CHANGE_RISE.getType()))
          .filter(ticker -> ticker.getAccTradePrice24h().compareTo(
                BigDecimal.valueOf(10_000_000_000L)) > 0)
          .filter(ticker -> ticker.getChangeRate() >= 0.03)
          .sorted(Comparator.comparing(TickerResponseDto::getSignedChangeRate).reversed())
          .toList();

    // filtered 된 데이터에서 market 필드 값만 추출하여 콤마로 구분된 문자열 생성
    List<String> popularMarkets = analyzedTickers.stream()
          .map(TickerResponseDto::getMarket)
          .filter(market -> market.startsWith("KRW"))
          .toList();

    // 보유 종목 조회
    List<AccountResponseDto> accounts = this.getAccount();
    List<String> holdingMarkets = accounts.stream()
          .filter(account -> !"KRW".equals(account.getCurrency()))
          .map(account -> account.getUnitCurrency() + "-" + account.getCurrency())
          .toList();

    List<String> defaultMarkets = List.of("KRW-BTC", "KRW-ETH", "KRW-XRP");

    // 중복 제거 및 리스트 통합
    Set<String> set = new HashSet<>();
    set.addAll(popularMarkets);
    set.addAll(holdingMarkets);
    set.addAll(defaultMarkets);

    // Set을 콤마로 구분된 문자열로 변환
    String scheduledMarket = String.join(",", set);

    Config config = new Config();
    config.setName("scheduled_market");
    config.setVal(scheduledMarket);

    configService.updateConfig(config);

    ColorfulConsoleOutput.printWithColor("종목 업데이트 완료: " + scheduledMarket,
          ColorfulConsoleOutput.GREEN);
  }
}
