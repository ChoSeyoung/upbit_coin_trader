package my.trader.coin.config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements WebFilter {

  private static final int MAX_REQUESTS_PER_SECOND = 30;
  private final AtomicInteger requestCount = new AtomicInteger(0);
  private Instant lastResetTime = Instant.now();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    Instant now = Instant.now();
    synchronized (this) {
      if (Duration.between(lastResetTime, now).getSeconds() >= 1) {
        requestCount.set(0);
        lastResetTime = now;
      }
      if (requestCount.incrementAndGet() > MAX_REQUESTS_PER_SECOND) {
        return Mono.delay(Duration.ofSeconds(1))
              .then(chain.filter(exchange));
      }
    }
    return chain.filter(exchange);
  }
}
