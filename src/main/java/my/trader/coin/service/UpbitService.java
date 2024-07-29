package my.trader.coin.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import my.trader.coin.dto.exchange.*;
import my.trader.coin.dto.quotation.CandleResponseDto;
import my.trader.coin.dto.quotation.CandleRequestDto;
import my.trader.coin.dto.quotation.TickerRequestDto;
import my.trader.coin.dto.quotation.TickerResponseDto;
import my.trader.coin.enums.Unit;
import my.trader.coin.enums.UpbitApi;
import my.trader.coin.enums.UpbitType;
import my.trader.coin.util.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UpbitService {
  private final AuthorizationGenerator authorizationGenerator;
  private final ExternalUtility externalUtility;
  private final ClosedOrderService closedOrderService;

  public UpbitService(AuthorizationGenerator authorizationGenerator,
                      ExternalUtility externalUtility,
                      ClosedOrderService closedOrderService) {
    this.authorizationGenerator = authorizationGenerator;
    this.externalUtility = externalUtility;
    this.closedOrderService = closedOrderService;
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
    String parameters = CharacterUtility.createQueryString(tickerRequestDto);

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
    String parameters = CharacterUtility.createQueryString(orderRequestDto);

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
    String parameters = CharacterUtility.createQueryString(candleRequestDto);

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

    String parameters = CharacterUtility.createQueryString(openOrderRequestDto);

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
    String parameters = CharacterUtility.createQueryString(cancelOrderRequestDto);

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
        // 매수 주문은 해당 메서드에서 처리하지 않음 (#23)
        if (openOrderResponseDto.getSide().equals("bid")) continue;

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

  public List<ClosedOrderResponseDto> initializeClosedOrders() {
    DateTimeFormatter timestampFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    // 최초 거래 시점
    // 최초 거래 시점 (UTC 기준으로 변환)
    OffsetDateTime startTimeKst =
          OffsetDateTime.of(2024, 6, 16, 22, 0, 0, 0, ZoneOffset.ofHours(9));
    OffsetDateTime startTime = startTimeKst.withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime endTime = startTime.plusHours(1).minusSeconds(1);

    // 거래내역 테이블 초기화
    closedOrderService.initClosedOrders();

    List<ClosedOrderResponseDto> allClosedOrders = new ArrayList<>();
    while (startTime.isBefore(OffsetDateTime.now(ZoneId.of("Asia/Seoul")))) {
      ClosedOrderRequestDto closedOrderRequestDto = ClosedOrderRequestDto.builder()
            .startTime(startTime.format(timestampFormatter))
            .endTime(endTime.format(timestampFormatter))
            .limit(1000)
            .orderBy("asc")
            .build();

      String url = UpbitApi.GET_CLOSED_ORDER.getUrl();
      String parameters = CharacterUtility.createQueryString(closedOrderRequestDto);

      URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

      String authorizationToken = authorizationGenerator.generateTokenWithParameter(
            closedOrderRequestDto);

      List<ClosedOrderResponseDto> response =
            externalUtility.getWithAuth(uri, ClosedOrderResponseDto.class, authorizationToken);

      if (response != null) {
        allClosedOrders.addAll(response);
        closedOrderService.saveClosedOrder(allClosedOrders);
      }

      // Increment the time range by 1 hour
      startTime = endTime.minusMinutes(1);
      endTime = startTime.plusHours(1).minusSeconds(1);

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
}
