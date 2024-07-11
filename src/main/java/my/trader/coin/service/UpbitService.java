package my.trader.coin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

    public JsonNode getTicker(String market) {
        String url = "/ticker?markets=" + market;
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

    // 추가적인 API 메서드를 여기에 추가할 수 있습니다.
}
