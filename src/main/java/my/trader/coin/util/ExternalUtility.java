package my.trader.coin.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.net.URI;
import java.util.List;
import my.trader.coin.enums.ColorfulConsoleOutput;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ExternalUtility {
  private final WebClient webClient;
  private final ObjectMapper objectMapper;

  public ExternalUtility(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  public <T> T deleteWithAuth(URI uri, Object requestBody, Class<T> responseType,
                              String authorizationToken) {
    ColorfulConsoleOutput.printWithColor("DELETE Request Uri: " + uri,
          ColorfulConsoleOutput.PURPLE);

    return webClient.delete()
          .uri(uri)
          .header("Content-Type", "application/json; charset=utf-8")
          .header("Authorization", authorizationToken)
          .retrieve()
          .onStatus(HttpStatusCode::isError, this::handleError)
          .bodyToMono(responseType)
          .block();
  }

  public <T> List<T> getWithoutAuth(URI uri, Class<T> responseType) {
     ColorfulConsoleOutput.printWithColor("GET Request Uri: " + uri, ColorfulConsoleOutput.PURPLE);

    return webClient.get()
          .uri(uri)
          .header("Content-Type", "application/json; charset=utf-8")
          .retrieve()
          .onStatus(HttpStatusCode::isError, this::handleError)
          .bodyToMono(String.class)
          .flatMap(json -> parseJsonList(json, responseType))
          .block();
  }

  public <T> List<T> getWithAuth(URI uri, Class<T> responseType, String authorizationToken) {
     ColorfulConsoleOutput.printWithColor("GET Request Uri: " + uri, ColorfulConsoleOutput.PURPLE);

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

  public <T> T postWithAuth(URI uri, Object requestBody, Class<T> responseType,
                           String authorizationToken) {
    ColorfulConsoleOutput.printWithColor("POST Request Uri: " + uri, ColorfulConsoleOutput.PURPLE);

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
