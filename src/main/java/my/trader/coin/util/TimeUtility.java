package my.trader.coin.util;

import org.springframework.stereotype.Component;

/**
 * 틱톡틱톡 시계는 와치 시계는 와치.
 */
@Component
public class TimeUtility {
  /**
   * 스레드는 자는중.
   * @param sec 몇초동안 재울까요?
   */
  public static void sleep(double sec) {
    try {
      Thread.sleep((long) (sec * 1000)); // 초를 밀리초로 변환
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
