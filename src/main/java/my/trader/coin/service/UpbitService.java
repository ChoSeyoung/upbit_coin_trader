package my.trader.coin.service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import my.trader.coin.config.AppConfig;
import my.trader.coin.dto.bootleg.UpbitMarketIndexTop10Dto;
import my.trader.coin.dto.exchange.*;
import my.trader.coin.dto.quotation.*;
import my.trader.coin.enums.*;
import my.trader.coin.util.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * UpbitService 클래스는 Upbit 거래소와의 통신을 통해 다양한 거래 데이터를 가져오고,
 * 주문 실행 및 취소 등의 거래 관련 기능을 제공합니다.
 */
@Service
public class UpbitService {
  private final AuthorizationGenerator authorizationGenerator;
  private final ExternalUtility externalUtility;

  /**
   * UpbitService 생성자
   *
   * @param authorizationGenerator 인증 토큰 생성기
   * @param externalUtility        외부 유틸리티 서비스
   */
  public UpbitService(AuthorizationGenerator authorizationGenerator,
                      ExternalUtility externalUtility) {
    this.authorizationGenerator = authorizationGenerator;
    this.externalUtility = externalUtility;
  }

  /**
   * 지원하는 시장 정보를 조회합니다.
   *
   * @return 지원하는 시장 정보의 리스트
   */
  public List<MarketResponseDto> getMarket() {
    MarketRequestDto marketRequestDto = MarketRequestDto.builder().isDetail(true).build();

    String url = UpbitApi.GET_MARKET.getUrl();
    String parameters = CharacterUtility.createQueryString(marketRequestDto, false);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    return externalUtility.getWithoutAuth(uri, MarketResponseDto.class);
  }

  /**
   * 계좌 정보를 조회합니다.
   *
   * @return 사용자의 계좌 정보 리스트
   */
  public List<AccountResponseDto> getAccount() {
    URI uri = UriComponentsBuilder.fromHttpUrl(UpbitApi.GET_ACCOUNT.getUrl()).build().toUri();

    String authorizationToken = authorizationGenerator.generateTokenWithoutParameter();

    return externalUtility.getWithAuth(uri, AccountResponseDto.class, authorizationToken);
  }

  /**
   * 지정된 시장의 현재 가격 정보를 조회합니다.
   *
   * @param markets 조회할 시장의 리스트
   * @return 각 시장의 현재 가격 정보 리스트
   */
  public List<TickerResponseDto> getTicker(List<String> markets) {
    if (markets == null || markets.isEmpty()) {
      return Collections.emptyList();
    }

    TickerRequestDto tickerRequestDto = TickerRequestDto.builder()
          .markets(String.join(",", markets))
          .build();

    String url = UpbitApi.GET_TICKER.getUrl();
    String parameters = CharacterUtility.createQueryString(tickerRequestDto, false);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    return externalUtility.getWithoutAuth(uri, TickerResponseDto.class);
  }

  /**
   * 지정한 조건에 따라 주문을 실행합니다.
   *
   * @param tickerSymbol 거래할 종목 코드
   * @param price        주문 가격
   * @param quantity     주문 수량
   * @param side         매수 또는 매도 방향
   * @return 주문 실행 결과
   */
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

  /**
   * 지정된 분 단위로 캔들 데이터를 조회합니다.
   *
   * @param market  시장 코드
   * @param unit    캔들 단위 (분 단위)
   * @param count   조회할 캔들 개수
   * @param orderBy 정렬 순서 ("asc" 또는 "desc")
   * @return 분 캔들 데이터 리스트
   */
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

  /**
   * 지정된 시장에서 미체결 주문 목록을 조회합니다.
   *
   * @param market 시장 코드
   * @return 미체결 주문 리스트
   */
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

  /**
   * 특정 주문을 취소합니다.
   *
   * @param uuid 주문의 UUID
   * @return 주문 취소 결과
   */
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

  /**
   * 작업 수행 전 미체결 주문을 취소합니다.
   *
   * @return 취소된 주문 리스트
   */
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

  /**
   * 작업 완료 후 매수 미체결 주문을 취소합니다.
   *
   * @return 취소된 주문 리스트
   */
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
        if (openOrderResponseDto.getSide().equals("ask")) {
          continue;
        }

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

  /**
   * RSI 지표를 계산하여 반환합니다.
   *
   * @param candles 캔들 리스트
   * @param weight  RSI 지표 계산을 위한 가중치
   * @return RSI 지표 값
   */
  public Double calculateRelativeStrengthIndex(List<CandleResponseDto> candles, int weight) {
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

  /**
   * ADX 지표를 계산하여 반환합니다.
   *
   * @param candles 캔들 리스트
   * @param weight  ADX 지표 계산을 위한 가중치
   * @return ADX 지표 값
   */
  public double calculateAverageDirectionalMovementIndex(List<CandleResponseDto> candles,
                                                         int weight) {
    int size = candles.size();
    if (size < weight * 2) {
      throw new IllegalArgumentException("캔들 데이터가 부족합니다.");
    }

    double[] tr = new double[size];
    double[] plusDM = new double[size];
    double[] minusDM = new double[size];
    double[] smoothedTR = new double[size];
    double[] smoothedPlusDM = new double[size];
    double[] smoothedMinusDM = new double[size];
    double[] plusDI = new double[size];
    double[] minusDI = new double[size];
    double[] dx = new double[size];
    double[] adx = new double[size];

    // 1. True Range (TR), +DM, -DM 계산
    for (int i = 1; i < size; i++) {
      CandleResponseDto prev = candles.get(i - 1);
      CandleResponseDto current = candles.get(i);

      double highDiff = current.getHighPrice() - prev.getHighPrice();
      double lowDiff = prev.getLowPrice() - current.getLowPrice();

      tr[i] = Math.max(
            current.getHighPrice() - current.getLowPrice(),
            Math.max(Math.abs(current.getHighPrice() - prev.getTradePrice()),
                  Math.abs(current.getLowPrice() - prev.getTradePrice()))
      );

      plusDM[i] = (highDiff > lowDiff && highDiff > 0) ? highDiff : 0;
      minusDM[i] = (lowDiff > highDiff && lowDiff > 0) ? lowDiff : 0;
    }

    // 2. 초기 Smoothed TR, +DM, -DM
    smoothedTR[weight - 1] = MathUtility.calculateSumInRange(tr, 1, weight);
    smoothedPlusDM[weight - 1] = MathUtility.calculateSumInRange(plusDM, 1, weight);
    smoothedMinusDM[weight - 1] = MathUtility.calculateSumInRange(minusDM, 1, weight);

    // 3. Smooth TR, +DM, -DM
    for (int i = weight; i < size; i++) {
      smoothedTR[i] = smoothedTR[i - 1] - (smoothedTR[i - 1] / weight) + tr[i];
      smoothedPlusDM[i] = smoothedPlusDM[i - 1] - (smoothedPlusDM[i - 1] / weight) + plusDM[i];
      smoothedMinusDM[i] = smoothedMinusDM[i - 1] - (smoothedMinusDM[i - 1] / weight) + minusDM[i];

      // +DI, -DI 계산
      plusDI[i] = (smoothedPlusDM[i] / smoothedTR[i]) * 100;
      minusDI[i] = (smoothedMinusDM[i] / smoothedTR[i]) * 100;

      // DX 계산
      dx[i] = (Math.abs(plusDI[i] - minusDI[i]) / (plusDI[i] + minusDI[i])) * 100;
    }

    // 4. ADX 계산
    adx[weight * 2 - 1] = MathUtility.calculateSumInRange(dx, weight, weight * 2) / weight;

    for (int i = weight * 2; i < size; i++) {
      adx[i] = ((adx[i - 1] * (weight - 1)) + dx[i]) / weight;
    }

    // 5. ADX 결과 반환
    return adx[adx.length - 1];
  }

  /**
   * 보유 종목을 스케줄링된 시장에 추가합니다.
   */
  public void addScheduledMarket() {
    // 거래대금 상위종목 포함 여부 결정 플래그 확인 후 종목 선정
    List<String> topTradingMarkets = new ArrayList<>();
    // 거래대금 상위 종목 거래 여부 체크 후 처리
    if (AppConfig.includeTopTradingStocks) {
      List<MarketResponseDto> marketResponses = this.getMarket();

      List<String> markets = marketResponses.stream()
            .map(MarketResponseDto::getMarket)
            .toList();

      // 종목 단위 현재가 정보 조회
      List<TickerResponseDto> tickers = this.getTicker(markets);

      // 24시간 누적 거래액 상위 10종목
      topTradingMarkets = tickers.stream()
            // 24시간 거래대금 기준 DESC
            .sorted(Comparator.comparing(TickerResponseDto::getAccTradePrice24h).reversed())
            // 전일대비 5% 이하 변동률을 가진 종목만 선택
            .filter(ticker -> ticker.getChangeRate() <= 0.05)
            // TickerResponseDto DTO 로 변환
            .map(TickerResponseDto::getMarket)
            // 변동률이 적은 USDT 코인은 스캘핑 대상 제외
            .filter(market -> !market.equals(MarketCode.KRW_USDT.getSymbol()))
            // 필터링된 종목중 상위 10개만 이용
            .limit(10)
            .toList();
    }

    // 현재 보유 잔고 조회
    List<AccountResponseDto> accounts = this.getAccount();
    // 현재 보유 잔고 리스트
    List<String> holdingMarkets = accounts.stream()
          // KRW 는 종목이 아니므로 필터링
          .filter(account -> !"KRW".equals(account.getCurrency()))
          // 보여종목 리스팅을 위하여 화폐단위 + 마켓코드 조합 처리
          .map(account -> account.getUnitCurrency() + "-" + account.getCurrency())
          .toList();

    // 중복 제거를 위한 Set 컬렉션 생성
    Set<String> set = new HashSet<>();
    // 선택된 매수 추가
    set.addAll(AppConfig.scheduledMarket);
    // 보여 종목 추가
    set.addAll(holdingMarkets);
    // 거래대금 상위 종목 추가
    set.addAll(topTradingMarkets);

    AppConfig.setScheduledMarket(new ArrayList<>(set));

    ColorfulConsoleOutput.printWithColor("종목 업데이트 완료: " + AppConfig.scheduledMarket,
          ColorfulConsoleOutput.GREEN);
  }

  /**
   * UBMI 10 조회.
   *
   * @return UBMI 10 변동률 값
   */
  public double getUpbitMarketIndexTop10() {
    String url = UpbitApi.GET_UPBIT_MARKET_INDEX_TOP10.getUrl();

    URI uri = UriComponentsBuilder.fromHttpUrl(url).build().toUri();

    UpbitMarketIndexTop10Dto result =
          externalUtility.getWithoutAuth(uri, UpbitMarketIndexTop10Dto.class).get(0);

    // 1. 상승률 조회 = (현재가 - 이전종가) / 이전종가 * 100
    double value = (result.getTradePrice() - result.getPrevClosingPrice()) / result.getPrevClosingPrice()
          * 100;

    // 2. 소수점 세 번째 자리에서 올림 처리
    double roundedUp = Math.ceil(value * 1000) / 1000;

    // 3. 소수점 두 번째 자리까지만 유지
    return Math.floor(roundedUp * 100) / 100;
  }

  /**
   * 구매 기준 ADX 를 구합니다.
   * 0.1 이상 값은 소숫점 첫번째 자리에서 올림처리 후 +5를 곱하여 기본 ADX 에 합산 (ADX * 5 = 5ADX)
   * -0.99 ~ 0.99 값은 기본 ADX 리턴
   * -1 이하 값은 소숫점 첫번째 자리에서 내림처리 후 -5를 곱하여 기본 ADX 에 합산 (-ADX * -5 = 5ADX)
   * @return 최소 구매 ADX 값
   */
  public double calculatePurchaseAdx() {
    double defaultAdx = 30.0;
    double ratio = AppConfig.upbitMarketIndexRatio;

    // -0.99 까지는 소숫점 첫번째 자리에서 올림처리
    // -1 부터
    int roundedValue;
    if (ratio > 0) {
      roundedValue = (int) Math.ceil(ratio);
    } else if (ratio > -1) {
      roundedValue = 0;
    } else {
      roundedValue = (int) Math.floor(ratio) * -1;
    }

    double multipliedValue = roundedValue * 5;

    return defaultAdx + multipliedValue;
  }
}