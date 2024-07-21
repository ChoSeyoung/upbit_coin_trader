package my.trader.coin.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import my.trader.coin.dto.order.*;
import my.trader.coin.enums.UpbitType;
import my.trader.coin.util.AuthorizationGenerator;
import my.trader.coin.util.Sejong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 업비트와 상호작용하여 시장 데이터를 가져옵니다.
 */
@Service
public class UpbitService {
  private static final Logger logger = LoggerFactory.getLogger(UpbitService.class);

  private final WebClient webClient;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final AuthorizationGenerator authorizationGenerator;
  private final Sejong sejong;

  /**
   * this is constructor.
   */
  public UpbitService(WebClient.Builder webClientBuilder,
                      AuthorizationGenerator authorizationGenerator, Sejong sejong) {
    this.webClient = webClientBuilder.build();
    this.restTemplate = new RestTemplate();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    this.authorizationGenerator = authorizationGenerator;
    this.sejong = sejong;
  }

  /**
   * 지정된 거래 쌍에 대한 티커 데이터를 가져옵니다.
   *
   * @param markets
   * @return JsonNode 형태의 티커 데이터
   */
  public List<TickerResponseDto> getTicker(List<String> markets) {
    List<TickerResponseDto> result = new ArrayList<>();

    String url = "https://api.upbit.com/v1/ticker";

    // TickerRequestDto 객체 생성
    TickerRequestDto tickerRequestDto = new TickerRequestDto();
    tickerRequestDto.setMarkets(markets);

    // 헤더
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json; charset=utf-8");

    // URL에 쿼리 파라미터 추가
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
          .queryParam("markets", String.join(",", markets));
    try {
      // URI 생성
      URI uri = uriBuilder.build().encode().toUri();

      // 요청 보내기
      HttpEntity<?> entity = new HttpEntity<>(headers);
      ResponseEntity<String> response = restTemplate.exchange(
            uri, HttpMethod.GET, entity, String.class);

      // JSON 문자열을 TickerResponseDto 리스트로 변환
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        String json = response.getBody();
        CollectionType javaType = objectMapper.getTypeFactory()
              .constructCollectionType(List.class, TickerResponseDto.class);
        result = objectMapper.readValue(json, javaType);
      }
    } catch (HttpClientErrorException e) {
      System.err.println("Error response: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("Unexpected error: " + e.getMessage());
    }

    return result;
  }

  /**
   * 매수 주문을 실행하는 코드.
   * <a href="https://docs.upbit.com/reference/%EC%A3%BC%EB%AC%B8%ED%95%98%EA%B8%B0">...</a>
   *
   * @param tickerSymbol 티커심볼
   * @param price        매수가격
   * @param quantity     수량
   * @return 매수주문성공여부
   */
  public OrderResponseDto executeBuyOrder(String tickerSymbol, double price, double quantity) {
    return executeOrder(tickerSymbol, price, quantity, UpbitType.ORDER_SIDE_BID.getType());
  }

  /**
   * 매도 주문을 실행하는 코드.
   * <a href="https://docs.upbit.com/reference/%EC%A3%BC%EB%AC%B8%ED%95%98%EA%B8%B0">...</a>
   *
   * @param tickerSymbol 티커심볼
   * @param price        매도가격
   * @param quantity     수량
   * @return 매도주문성공여부
   */
  public OrderResponseDto executeSellOrder(String tickerSymbol, double price, double quantity) {
    return executeOrder(tickerSymbol, price, quantity, UpbitType.ORDER_SIDE_ASK.getType());
  }

  /**
   * identifier 를 이용하여 현재 주문상태를 확인합니다.
   *
   * @param identifiers 식별자
   * @return 주문리스트
   */
  public List<OrderStatusResponseDto> getOrderStatusByIds(String tickerSymbol,
                                                          List<String> identifiers) {
    List<OrderStatusResponseDto> result = new ArrayList<>();

    String url = "https://api.upbit.com/v1/orders";

    // OrderStatusRequestDto 객체 생성
    OrderStatusRequestDto orderStatusRequestDto = OrderStatusRequestDto.builder()
          .market(tickerSymbol)
          .uuids(identifiers)
          .build();

    // 헤더
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json; charset=utf-8");
    headers.set("Authorization",
          authorizationGenerator.generateTokenWithParameter(orderStatusRequestDto));

    // 파라미터를 URL에 추가
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
    try {
      // OrderRequestDto 객체의 필드를 읽어 URI 빌더에 추가
      Field[] fields = orderStatusRequestDto.getClass().getDeclaredFields();
      for (Field field : fields) {
        field.setAccessible(true);
        Object value = field.get(orderStatusRequestDto);
        if (value != null) {
          uriBuilder.queryParam(field.getName(), value.toString());
        }
      }

      // URI 생성
      URI uri = uriBuilder.build().encode().toUri();

      // 요청 보내기
      HttpEntity<?> entity = new HttpEntity<>(headers);
      ResponseEntity<String> response = restTemplate.exchange(
            uri, HttpMethod.POST, entity, String.class);

      // JSON 문자열을 OrderStatusResponseDto 리스트로 변환
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        String json = response.getBody();
        CollectionType javaType = objectMapper.getTypeFactory()
              .constructCollectionType(List.class, OrderStatusResponseDto.class);
        result = objectMapper.readValue(json, javaType);
      }
    } catch (HttpClientErrorException e) {
      System.err.println("Error response: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("Unexpected error: " + e.getMessage());
    }

    return result;
  }

  /**
   * 업비트 주문하기 API 호출 공통 메서드.
   *
   * @param tickerSymbol 티커심볼
   * @param price        매수/매도 가격
   * @param quantity     매수/매도 수량
   * @param side         매수/매도 결정 타입
   * @return 매수/매도 성공시 true 응답
   */
  public OrderResponseDto executeOrder(String tickerSymbol, double price, double quantity, String side) {
    String url = "https://api.upbit.com/v1/orders";

    // OrderRequestDto 객체 생성
    OrderRequestDto orderRequestDto = OrderRequestDto.builder()
          .market(tickerSymbol)
          .side(side)
          .volume(quantity)
          .price(price)
          .ordType(UpbitType.ORDER_TYPE_LIMIT.getType())
          .build();

    // 헤더 설정
    String authorizationToken = authorizationGenerator.generateTokenWithParameter(orderRequestDto);

    // URL 파라미터 추가
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
    Field[] fields = orderRequestDto.getClass().getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      Object value;
      try {
        value = field.get(orderRequestDto);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e.getMessage());
      }
      if (value != null) {
        uriBuilder.queryParam(sejong.camelToSnakeCase(field.getName()), value);
      }
    }

    // URI 생성
    URI uri = uriBuilder.build().encode().toUri();

    // WebClient를 사용하여 비동기적으로 요청 보내기
    OrderResponseDto result = webClient.post()
          .uri(uri)
          .header("Content-Type", "application/json; charset=utf-8")
          .header("Authorization", authorizationToken)
          .retrieve()
          .bodyToMono(OrderResponseDto.class)
          .block();

    return result;
  }
}
