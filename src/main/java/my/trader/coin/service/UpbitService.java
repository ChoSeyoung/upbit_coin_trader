package my.trader.coin.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import my.trader.coin.dto.exchange.*;
import my.trader.coin.dto.quotation.CandleResponseDto;
import my.trader.coin.dto.quotation.CandleRequestDto;
import my.trader.coin.enums.UpbitType;
import my.trader.coin.util.AuthorizationGenerator;
import my.trader.coin.util.Sejong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
public class UpbitService {
  private static final Logger logger = LoggerFactory.getLogger(UpbitService.class);
  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final AuthorizationGenerator authorizationGenerator;
  private final Sejong sejong;

  public UpbitService(WebClient.Builder webClientBuilder,
                      AuthorizationGenerator authorizationGenerator,
                      Sejong sejong) {
    this.webClient = webClientBuilder.build();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    this.authorizationGenerator = authorizationGenerator;
    this.sejong = sejong;
  }

  public List<TickerResponseDto> getTicker(List<String> markets) {
    URI uri =
          buildUri("https://api.upbit.com/v1/ticker", Map.of("markets", String.join(",", markets)));
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
    URI uri = buildUriWithParams("https://api.upbit.com/v1/orders", orderRequestDto);
    String authorizationToken = authorizationGenerator.generateTokenWithParameter(orderRequestDto);
    return performPostRequest(uri, orderRequestDto, OrderResponseDto.class, authorizationToken);
  }

  public List<Double> getClosePrices(String market, int count) {
    CandleRequestDto candleRequestDto = CandleRequestDto.builder()
          .market(market)
          .count(count)
          .build();

    URI uri = buildUriWithParams("https://api.upbit.com/v1/candles/minutes/1", candleRequestDto);
    List<CandleResponseDto> candleResponseDtos = performGetRequest(uri, CandleResponseDto.class);
    List<Double> closePrices = new ArrayList<>();
    for (CandleResponseDto candleResponseDto : candleResponseDtos) {
      closePrices.add(candleResponseDto.getTradePrice());
    }
    return closePrices;
  }

  private <T> List<T> performGetRequest(URI uri, Class<T> responseType) {
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

  private URI buildUri(String baseUrl, Map<String, Object> queryParams) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl);
    queryParams.forEach(uriBuilder::queryParam);
    return uriBuilder.build().encode().toUri();
  }

  private URI buildUriWithParams(String baseUrl, Object params) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl);
    Field[] fields = params.getClass().getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      try {
        Object value = field.get(params);
        if (value != null) {
          String fieldName = sejong.camelToSnakeCase(field.getName());
          uriBuilder.queryParam(fieldName, value);
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e.getMessage());
      }
    }
    return uriBuilder.build().encode().toUri();
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
