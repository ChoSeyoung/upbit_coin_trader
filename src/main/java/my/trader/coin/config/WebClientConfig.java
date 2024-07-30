package my.trader.coin.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient.Builder webClientBuilder() {
    // 커넥션 프로바이더 설정
    ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
          .maxConnections(1000) // maxConnection 설정
          .maxIdleTime(Duration.ofSeconds(20)) // 유휴 커넥션 제거 주기 설정
          .maxLifeTime(Duration.ofSeconds(60)) // 커넥션의 최대 수명 설정
          .lifo() // LIFO 설정
          .build();

    // HttpClient 설정
    HttpClient httpClient = HttpClient.create(connectionProvider)
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
          .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(10))
                      .addHandlerLast(new WriteTimeoutHandler(10)))
          .resolver(DefaultAddressResolverGroup.INSTANCE); // 기본 주소 해석기 사용

    // Rate Limiter 설정
    RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
          .limitRefreshPeriod(Duration.ofSeconds(1))
          .limitForPeriod(30)
          .timeoutDuration(Duration.ofMillis(0))
          .build();

    RateLimiter rateLimiter = RateLimiter.of("custom", rateLimiterConfig);

    // WebClient 설정
    return WebClient.builder()
          .clientConnector(new ReactorClientHttpConnector(httpClient))
          .filter(ExchangeFilterFunction.ofRequestProcessor(Mono::just))
          .filter(ExchangeFilterFunction.ofResponseProcessor(Mono::just))
          .filter((request, next) -> {
            return Mono.defer(() -> {
              if (rateLimiter.acquirePermission()) {
                return next.exchange(request);
              } else {
                return Mono.delay(Duration.ofSeconds(1))
                      .then(next.exchange(request));
              }
            })
            .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
            .onErrorResume(Mono::error);
          })
          .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(16 * 1024 * 1024)); // 버퍼 크기를 16MB로 설정
  }
}
