package my.trader.coin.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import my.trader.coin.dto.exchange.*;
import my.trader.coin.dto.quotation.CandleResponseDto;
import my.trader.coin.dto.quotation.CandleRequestDto;
import my.trader.coin.dto.quotation.TickerRequestDto;
import my.trader.coin.dto.quotation.TickerResponseDto;
import my.trader.coin.enums.UpbitApi;
import my.trader.coin.enums.UpbitType;
import my.trader.coin.util.AuthorizationGenerator;
import my.trader.coin.util.CharacterUtility;
import my.trader.coin.util.ExternalUtility;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UpbitService {
  private final AuthorizationGenerator authorizationGenerator;
  private final ExternalUtility externalUtility;

  public UpbitService(AuthorizationGenerator authorizationGenerator, ExternalUtility externalUtility) {
    this.authorizationGenerator = authorizationGenerator;
    this.externalUtility = externalUtility;
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

    return externalUtility.postWithAuth(uri, orderRequestDto, OrderResponseDto.class, authorizationToken);
  }

  public List<Double> getClosePrices(String market, int count) {
    CandleRequestDto candleRequestDto = CandleRequestDto.builder()
          .market(market)
          .count(count)
          .build();

    String url = UpbitApi.GET_MINUTE_CANDLE.getUrl();
    String parameters = CharacterUtility.createQueryString(candleRequestDto);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    List<CandleResponseDto> candleResponseDtos =
          externalUtility.getWithoutAuth(uri, CandleResponseDto.class);
    List<Double> closePrices = new ArrayList<>();
    for (CandleResponseDto candleResponseDto : candleResponseDtos) {
      closePrices.add(candleResponseDto.getTradePrice());
    }
    return closePrices;
  }

  public List<OpenOrderResponseDto> getOpenOrders(String market) {
    OpenOrderRequestDto openOrderRequestDto = OpenOrderRequestDto.builder()
          .market(market).build();

    String url = UpbitApi.GET_OPEN_ORDER.getUrl();

    String parameters = CharacterUtility.createQueryString(openOrderRequestDto);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    String authorizationToken = authorizationGenerator.generateTokenWithParameter(openOrderRequestDto);

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
}
