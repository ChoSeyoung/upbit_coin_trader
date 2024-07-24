package my.trader.coin.config;

import java.util.List;
import java.util.Objects;
import my.trader.coin.model.Config;
import my.trader.coin.service.ConfigService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 *
 */
@Configuration
public class CacheConfig {

  private final CacheManager cacheManager;
  private final ConfigService configService;

  public CacheConfig(CacheManager cacheManager, ConfigService configService) {
    this.cacheManager = cacheManager;
    this.configService = configService;
  }

  @Bean
  public ApplicationRunner initializer() {
    return args -> {
      List<Config> configs = configService.loadAllConfigs();
      configs.forEach(config -> {
        Objects.requireNonNull(cacheManager.getCache("configs")).put(config.getName(), config);
      });
    };
  }
}
