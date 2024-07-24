package my.trader.coin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * just run.
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class CoinApplication {

  public static void main(String[] args) {
    SpringApplication.run(CoinApplication.class, args);
  }

}
