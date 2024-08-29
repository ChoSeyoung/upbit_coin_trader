package my.trader.coin;

import my.trader.coin.service.UpbitService;
import my.trader.coin.util.WebScraper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;

/**
 * just run.
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class CoinApplication {

  private final WebScraper webScraper;
  private final UpbitService upbitService;

  public CoinApplication(WebScraper webScraper, UpbitService upbitService) {
    this.webScraper = webScraper;
    this.upbitService = upbitService;
  }

  public static void main(String[] args) {
    SpringApplication.run(CoinApplication.class, args);
  }

  /**
   * 서버 실행 직후 초기화 작업.
   *
   * @return CommandLineRunner
   */
  @Bean
  public CommandLineRunner init() {
    return args -> {
      // UBMI 지수 초기 값 설정.
      webScraper.fetchUpbitMarketIndexRatio();

      // 종목 초기 설정
      webScraper.fetchHighMarketCapitalization();
      upbitService.addScheduledMarket();

      // something else
    };
  }
}
