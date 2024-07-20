package my.trader.coin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import my.trader.coin.dto.order.OrderRequestDto;
import my.trader.coin.dto.order.OrderStatusRequestDto;
import my.trader.coin.enums.UpbitType;
import my.trader.coin.util.AuthorizationGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * 업비트와 상호작용하여 시장 데이터를 가져옵니다.
 */
@Service
public class UpbitService {
  private static final Logger logger = LoggerFactory.getLogger(UpbitService.class);

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final AuthorizationGenerator authorizationGenerator;

  /**
   * this is constructor.
   */
  public UpbitService(AuthorizationGenerator authorizationGenerator) {
    this.restTemplate = new RestTemplate();
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
    String url = String.format("https://api.upbit.com/v1/ticker?markets=%s", symbol);
    try {
      String response = restTemplate.getForObject(url, String.class);
      return objectMapper.readTree(response);
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
      return executeOrder(tickerSymbol, price, quantity, identifier,
            UpbitType.ORDER_SIDE_BID.getType());
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
      return executeOrder(tickerSymbol, price, quantity, identifier,
            UpbitType.ORDER_SIDE_ASK.getType());
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

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("application/json; charset=utf-8"));
    headers.set("Authorization",
          authorizationGenerator.generateTokenWithParameter(orderStatusRequestDto));

    HttpEntity<String> entity = new HttpEntity<>(headers);

    StringBuilder url = new StringBuilder(
          String.format("https://api.upbit.com/v1/orders/uuids?market=%s", tickerSymbol)
    );
    for (String identifier : identifiers) {
      url.append("&identifiers[]=").append(identifier);
    }

    try {
      ResponseEntity<String> response =
            restTemplate.exchange(url.toString(), HttpMethod.GET, entity, String.class);
      return objectMapper.readTree(response.getBody());
    } catch (Exception e) {
      throw new RuntimeException("Error fetching order status", e);
    }
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
    String url = "https://api.upbit.com/v1/orders";

    // OrderRequestDto 객체 생성
    OrderRequestDto orderRequestDto = OrderRequestDto.builder()
          .market(tickerSymbol)
          .side(side)
          .volume(quantity)
          .price(price)
          .ordType(UpbitType.ORDER_TYPE_LIMIT.getType())
          .identifier(identifier)
          .build();

    // 헤더
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("application/json; charset=utf-8"));
    headers.set("Authorization",
          authorizationGenerator.generateTokenWithParameter(orderRequestDto));

    // 바디
    HttpEntity<OrderRequestDto> entity = new HttpEntity<>(orderRequestDto, headers);

    System.out.println(entity);
    try {
      ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
      System.out.println("Response: " + response.getBody());
      return true;
    } catch (HttpClientErrorException e) {
      System.err.println("Error response: " + e.getMessage());
      return false;
    } catch (Exception e) {
      System.err.println("Unexpected error: " + e.getMessage());
      return false;
    }
  }
}
