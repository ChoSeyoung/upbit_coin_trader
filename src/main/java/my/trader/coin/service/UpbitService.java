package my.trader.coin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.trader.coin.enums.TickerSymbol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
     * @param market 거래 쌍을 나타내는 enum 값
     * @return JsonNode 형태의 티커 데이터
     */
    public JsonNode getTicker(TickerSymbol market) {
        String url = "/ticker?markets=" + market.getSymbol();
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
}
