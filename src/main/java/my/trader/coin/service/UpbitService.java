package my.trader.coin.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import my.trader.coin.dto.exchange.*;
import my.trader.coin.dto.quotation.CandleResponseDto;
import my.trader.coin.dto.quotation.CandleRequestDto;
import my.trader.coin.dto.quotation.TickerRequestDto;
import my.trader.coin.dto.quotation.TickerResponseDto;
import my.trader.coin.enums.ColorfulConsoleOutput;
import my.trader.coin.enums.UpbitType;
import my.trader.coin.util.AuthorizationGenerator;
import my.trader.coin.util.Sejong;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
public class UpbitService {
  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final AuthorizationGenerator authorizationGenerator;

  public UpbitService(WebClient.Builder webClientBuilder,
                      AuthorizationGenerator authorizationGenerator) {
    this.webClient = webClientBuilder.build();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    this.authorizationGenerator = authorizationGenerator;
  }

  public List<TickerResponseDto> getTicker(List<String> markets) {
    TickerRequestDto tickerRequestDto = TickerRequestDto.builder()
          .markets(String.join(",", markets))
          .build();

    String url = "https://api.upbit.com/v1/ticker";
    String parameters = Sejong.createQueryString(tickerRequestDto);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    return performGetRequest(uri, TickerResponseDto.class);
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

    String url = "https://api.upbit.com/v1/orders";
    String parameters = Sejong.createQueryString(orderRequestDto);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    String authorizationToken = authorizationGenerator.generateTokenWithParameter(orderRequestDto);
    return performPostRequest(uri, orderRequestDto, OrderResponseDto.class, authorizationToken);
  }

  public List<Double> getClosePrices(String market, int count) {
    CandleRequestDto candleRequestDto = CandleRequestDto.builder()
          .market(market)
          .count(count)
          .build();

    String url = "https://api.upbit.com/v1/candles/minutes/1";
    String parameters = Sejong.createQueryString(candleRequestDto);

    URI uri = UriComponentsBuilder.fromHttpUrl(url + "?" + parameters).build().toUri();

    List<CandleResponseDto> candleResponseDtos = performGetRequest(uri, CandleResponseDto.class);
    List<Double> closePrices = new ArrayList<>();
    for (CandleResponseDto candleResponseDto : candleResponseDtos) {
      closePrices.add(candleResponseDto.getTradePrice());
    }
    return closePrices;
  }

  private <T> List<T> performGetRequest(URI uri, Class<T> responseType) {
    ColorfulConsoleOutput.printWithColor("Request Uri: " + uri, ColorfulConsoleOutput.PURPLE);

    return webClient.get()
          .uri(uri)
          .header("Content-Type", "application/json; charset=utf-8")
          .retrieve()
          .onStatus(HttpStatusCode::isError, this::handleError)
          .bodyToMono(String.class)
          .flatMap(json -> parseJsonList(json, responseType))
          .block();
  }

  private <T> List<T> performGetRequest(URI uri, Class<T> responseType, String authorizationToken) {
    ColorfulConsoleOutput.printWithColor("Request Uri: " + uri, ColorfulConsoleOutput.PURPLE);

    return webClient.get()
          .uri(uri)
          .header("Content-Type", "application/json; charset=utf-8")
          .header("Authorization", authorizationToken)
          .retrieve()
          .onStatus(HttpStatusCode::isError, this::handleError)
          .bodyToFlux(responseType)
          .collectList()
          .block();
  }

  private <T> T performPostRequest(URI uri, Object requestBody, Class<T> responseType,
                                   String authorizationToken) {
    return webClient.post()
          .uri(uri)
          .header("Content-Type", "application/json; charset=utf-8")
          .header("Authorization", authorizationToken)
          .bodyValue(requestBody)
          .retrieve()
          .onStatus(HttpStatusCode::isError, this::handleError)
          .bodyToMono(responseType)
          .block();
  }

  private Mono<Throwable> handleError(ClientResponse response) {
    return response.bodyToMono(String.class)
          .flatMap(errorBody -> {
            System.err.println("Error response: " + errorBody);
            return Mono.error(new RuntimeException("API call failed: " + errorBody));
          });
  }

  private <T> Mono<List<T>> parseJsonList(String json, Class<T> elementType) {
    try {
      CollectionType javaType =
            objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
      List<T> result = objectMapper.readValue(json, javaType);
      return Mono.just(result);
    } catch (Exception e) {
      return Mono.error(new RuntimeException("Failed to parse response: " + e.getMessage(), e));
    }
  }
}
