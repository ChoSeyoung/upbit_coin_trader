package my.trader.coin.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class JvmOptionsConfig {
  @PostConstruct
  public void setJvmOptions() {
    System.setProperty("Xms", "512m");
    System.setProperty("Xmx", "2048m");
    System.setProperty("XX:+UseG1GC", "true");
  }
}
