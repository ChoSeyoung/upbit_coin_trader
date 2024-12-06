package my.trader.coin.util;

import java.util.List;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.springframework.stereotype.Component;

/**
 * 귀찮은 수학공식들을 풀어주는 메서드들의 집합체입니다.
 * <a href="https://ko.wikipedia.org/wiki/%ED%83%88%EB%A0%88%EC%8A%A4">...</a>
 */
@Component
public class MathUtility {
  /**
   * 소수점 8자리까지 계산하고 그 뒤는 절사하며, 소수점 8자리에서 무조건 올림 처리합니다.
   *
   * @param minimumOrderAmount 최소 주문금액
   * @param currentPrice       현재가격
   * @return 최소주문수량
   */
  public static Double calculateMinimumOrderQuantity(Double minimumOrderAmount,
                                                     Double currentPrice) {
    return minimumOrderAmount / currentPrice;
  }

  /**
   * EMA(Exponential Moving Average) 계산
   * 정식 공식 : 2 / 1 + weight
   * 업비트 공식 : 1 / (1 + (weight - 1))
   *
   * @param data 상승/하락 갭 데이터
   * @return EMA
   */
  public static double calculateExponentialMovingAverage(List<Double> data, int weight) {
    //
    // 업비트에서 사용하는 수식은
    double formula = (double) 1 / (1 + (weight - 1));

    double result = 0;
    if (!data.isEmpty()) {
      result = data.getFirst();
      if (data.size() > 1) {
        for (int i = 1; i < data.size(); i++) {
          result = (data.get(i) * formula) + (result * (1 - formula));
        }
      }
    }

    return result;
  }
}
