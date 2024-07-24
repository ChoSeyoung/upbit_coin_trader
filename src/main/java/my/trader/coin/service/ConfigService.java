package my.trader.coin.service;

import my.trader.coin.model.Config;
import my.trader.coin.repository.ConfigRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {

  private final ConfigRepository configRepository;

  public ConfigService(ConfigRepository configRepository) {
    this.configRepository = configRepository;
  }

  @Cacheable("configs")
  public Config getConfByName(String name) {
    return configRepository.findById(name).orElse(null);
  }

  public List<Config> loadAllConfigs() {
    return configRepository.findAll();
  }
}
