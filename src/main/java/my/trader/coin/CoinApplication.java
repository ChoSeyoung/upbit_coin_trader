package my.trader.coin;

import my.trader.coin.service.UpbitService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * just run.
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class CoinApplication {

  private final UpbitService upbitService;

  public CoinApplication(UpbitService upbitService) {
    this.upbitService = upbitService;
  }

  public static void main(String[] args) {
    System.out.println(System.getenv("UPBIT_ACCESS_KEY"));
    System.out.println(System.getenv("UPBIT_SECRET_KEY"));
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
      upbitService.addScheduledMarket();
    };
  }
}
