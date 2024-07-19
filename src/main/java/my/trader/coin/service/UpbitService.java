package my.trader.coin.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import my.trader.coin.dto.order.OrderRequestDto;
import my.trader.coin.dto.order.OrderStatusRequestDto;
import my.trader.coin.util.AuthorizationGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * 업비트와 상호작용하여 시장 데이터를 가져옵니다.
 */
@Service
public class UpbitService {
  private static final Logger logger = LoggerFactory.getLogger(UpbitService.class);

  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final AuthorizationGenerator authorizationGenerator;

  /**
   * this is constructor.
   */
  public UpbitService(AuthorizationGenerator authorizationGenerator) {
    this.webClient = WebClient.builder()
          .baseUrl("https://api.upbit.com")
          .build();
    this.objectMapper = new ObjectMapper();
    this.authorizationGenerator = authorizationGenerator;
  }

  /**
   * 지정된 거래 쌍에 대한 티커 데이터를 가져옵니다.
   *
   * @param symbol 티커심볼
   * @return JsonNode 형태의 티커 데이터
   */
  public JsonNode getTicker(String symbol) {
    String url = String.format("/v1/ticker?markets=%s", symbol);
    Mono<String> response = webClient.get()
          .uri(url)
          .retrieve()
          .bodyToMono(String.class);

    try {
      return objectMapper.readTree(response.block());
    } catch (Exception e) {
      throw new RuntimeException("Error fetching ticker data", e);
    }
  }

  /**
   * 매수 주문을 실행하는 코드.
   * <a href="https://docs.upbit.com/reference/%EC%A3%BC%EB%AC%B8%ED%95%98%EA%B8%B0">...</a>
   *
   * @param tickerSymbol   티커심볼
   * @param price          매수가격
   * @param quantity       수량
   * @param identifier     식별자
   * @param simulationMode 시뮬레이션모드 여부
   * @return 매수주문성공여부
   */
  public boolean executeBuyOrder(String tickerSymbol, double price, double quantity,
                                 String identifier, boolean simulationMode) {
    if (simulationMode) {
      return true;
    } else {
      return executeOrder(tickerSymbol, price, quantity, identifier, "bid");
    }
  }

  /**
   * 매도 주문을 실행하는 코드.
   * <a href="https://docs.upbit.com/reference/%EC%A3%BC%EB%AC%B8%ED%95%98%EA%B8%B0">...</a>
   *
   * @param tickerSymbol   티커심볼
   * @param price          매도가격
   * @param quantity       수량
   * @param identifier     식별자
   * @param simulationMode 시뮬레이션모드 여부
   * @return 매도주문성공여부
   */
  public boolean executeSellOrder(String tickerSymbol, double price, double quantity,
                                  String identifier, boolean simulationMode) {
    if (simulationMode) {
      return true;
    } else {
      return executeOrder(tickerSymbol, price, quantity, identifier, "ask");
    }
  }

  /**
   * identifier 를 이용하여 현재 주문상태를 확인합니다.
   *
   * @param identifiers 식별자
   * @return 주문리스트
   */
  public JsonNode getOrderStatusByIds(String tickerSymbol, List<String> identifiers) {
    OrderStatusRequestDto orderStatusRequestDto = OrderStatusRequestDto.builder()
          .market(tickerSymbol)
          .identifiers(identifiers)
          .build();

    String authorizationHeader = authorizationGenerator.generateTokenWithParameter(orderStatusRequestDto);

    return webClient.get()
          .uri(uriBuilder -> {
            uriBuilder.path("/v1/orders/uuids");
            uriBuilder.queryParam("market", tickerSymbol);
            for (String identifier : identifiers) {
              uriBuilder.queryParam("identifiers[]", identifier);
            }
            return uriBuilder.build();
          })
          .header("Authorization", authorizationHeader)
          .retrieve()
          .bodyToMono(JsonNode.class)
          .block();
  }

  /**
   * 업비트 주문하기 API 호출 공통 메서드.
   *
   * @param tickerSymbol 티커심볼
   * @param price        매수/매도 가격
   * @param quantity     매수/매도 수량
   * @param identifier   식별자
   * @param side         매수/매도 결정 타입
   * @return 매수/매도 성공시 true 응답
   */
  private boolean executeOrder(String tickerSymbol, double price, double quantity,
                               String identifier, String side) {
    String url = "/v1/orders";

    // OrderRequestDto 객체 생성
    OrderRequestDto orderRequestDto = OrderRequestDto.builder()
          .market(tickerSymbol)
          .side(side)
          .volume(quantity)
          .price(price)
          .ordType("limit")
          .identifier(identifier)
          .build();

    String authorizationHeader = authorizationGenerator.generateTokenWithParameter(orderRequestDto);
    // WebClient 를 사용하여 요청을 보내고 응답을 받음
    Mono<String> response = webClient.post()
          .uri(url)
          .header("Authorization", authorizationHeader)
          .contentType(MediaType.APPLICATION_JSON)
          .body(Mono.just(orderRequestDto), OrderRequestDto.class)
          .retrieve()
          .bodyToMono(String.class)
          .doOnError(e -> {
            // 에러 처리 로직
            System.err.println("Error occurred: " + e.getMessage());
          })
          .doOnSuccess(res -> {
            // 성공 처리 로직
            System.out.println("Order placed successfully: " + res);
          });

    // 응답을 구독
    response.subscribe(
          res -> System.out.println("Response: " + res),
          err -> {
            if (err instanceof WebClientResponseException webClientResponseException) {
              System.err.println(
                    "Error response: " + webClientResponseException.getResponseBodyAsString());
            } else {
              System.err.println("Unexpected error: " + err.getMessage());
            }
          }
    );

    return true;
  }
}
