package my.trader.coin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.trader.coin.enums.TickerSymbol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Upbit API와 상호작용하여 시장 데이터를 가져옵니다.
 */
@Service
public class UpbitService {

  private final WebClient webClient;
  private final ObjectMapper objectMapper;

  @Value("${upbit.api.key}")
  private String apiKey;

  @Value("${upbit.secret.key}")
  private String secretKey;

  public UpbitService() {
    this.webClient = WebClient.builder()
          .baseUrl("https://api.upbit.com/v1")
          .build();
    this.objectMapper = new ObjectMapper();
  }

  /**
   * 지정된 거래 쌍에 대한 티커 데이터를 가져옵니다.
   *
   * @param symbol 티커심볼
   * @return JsonNode 형태의 티커 데이터
   */
  public JsonNode getTicker(String symbol) {
    String url = String.format("/ticker?markets=%s", symbol);
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

  public boolean executeBuyOrder(String market, BigDecimal price, double quantity) {
    // 업비트 API를 호출하여 매수 주문을 실행하는 코드
    // 실제 구현시에는 업비트 API의 요구사항에 맞게 작성해야 함
    // 아래는 가상의 예시 코드이므로 실제 업비트 API를 호출하도록 수정해야 합니다.

    // 실제 업비트 API 호출 예시
    // String apiUrl = "https://api.upbit.com/v1/orders";
    // HttpHeaders headers = new HttpHeaders();
    // headers.set("Authorization", "Bearer " + apiKey);
    // HttpEntity<YourRequestDto> request = new HttpEntity<>(yourRequestDto, headers);
    // ResponseEntity<YourResponseDto> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, YourResponseDto.class);

    // 여기서는 간소화를 위해 단순히 true를 반환하도록 하겠습니다.
    return true;
  }

  public boolean executeSellOrder(String market, BigDecimal price, double quantity) {
    // 업비트 API를 호출하여 매도 주문을 실행하는 코드
    // 실제 구현시에는 업비트 API의 요구사항에 맞게 작성해야 함
    // 아래는 가상의 예시 코드이므로 실제 업비트 API를 호출하도록 수정해야 합니다.

    // 실제 업비트 API 호출 예시
    // String apiUrl = "https://api.upbit.com/v1/orders";
    // HttpHeaders headers = new HttpHeaders();
    // headers.set("Authorization", "Bearer " + apiKey);
    // HttpEntity<YourRequestDto> request = new HttpEntity<>(yourRequestDto, headers);
    // ResponseEntity<YourResponseDto> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, YourResponseDto.class);

    // 여기서는 간소화를 위해 단순히 true를 반환하도록 하겠습니다.
    return true;
  }
}
