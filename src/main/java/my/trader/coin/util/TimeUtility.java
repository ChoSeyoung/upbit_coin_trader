package my.trader.coin.util;

import org.springframework.stereotype.Component;

/**
 * 틱톡틱톡 시계는 와치 시계는 와치
 */
@Component
public class TimeUtility {
  public static void sleep(long sec) {
    try {
      Thread.sleep(sec * 1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
