package my.trader.coin.service;

import my.trader.coin.model.Config;
import my.trader.coin.repository.ConfigRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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

  @CacheEvict(value = "configs", key = "#config.name")
  public void evictConfigCache(Config config) {
    // 이 메서드는 캐시를 비우기 위한 것이므로 내부에 특별한 코드가 필요하지 않습니다.
  }

  @CachePut(value = "configs", key = "#config.name")
  public Config updateConfigCache(Config config) {
    return config;
  }

  public Config updateConfig(Config config) {
    Config updatedConfig = configRepository.save(config);
    evictConfigCache(updatedConfig);
    return updateConfigCache(updatedConfig);
  }

  @CacheEvict(value = "configs", key = "#name")
  public void deleteConfig(String name) {
    configRepository.deleteById(name);
  }
}
