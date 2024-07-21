package my.trader.coin.util;

import org.springframework.stereotype.Component;

/**
 * 우리의 탈레스는 귀찮은 수학공식들을 풀어주는 메서드들의 집합체입니다.
 * <a href="https://ko.wikipedia.org/wiki/%ED%83%88%EB%A0%88%EC%8A%A4">...</a>
 */
@Component
public class Thales {
  /**
   * 주어진 x 값을 곱하여 min 을 초과하도록 하는 횟수를 구하는 메서드.
   *
   * @param min 최소값 (ex. 최소주문금액)
   * @param x   기준값 (ex. 1주당 주문금액)
   * @return 횟수
   */
  public static int calculateMultiplicationCount(double min, double x) {
    int count = 0;
    double result = x;

    while (result <= min) {
      result *= x;
      count++;
    }

    return count;
  }
}
