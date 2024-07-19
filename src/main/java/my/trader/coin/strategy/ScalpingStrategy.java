package my.trader.coin.strategy;

import java.util.LinkedList;
import java.util.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 스캘핑 전략을 이용하여 매매를 진행합니다.
 */
@Service
public class ScalpingStrategy {

  // 가격 이동 평균을 계산할 창 크기
  private static final int WINDOW_SIZE = 5;

  // 최근 가격을 저장하는 큐
  private final Queue<Double> priceWindow = new LinkedList<>();

  // 최근 거래량을 저장하는 큐
  private final Queue<Double> volumeWindow = new LinkedList<>();

  // 손절매 및 목표 이익 비율을 설정 (수정 불가)
  @Value("${upbit.ratio.profit}")
  private Double profitRatio;
  @Value("${upbit.ratio.loss}")
  private Double loseRatio;
  // 진입 가격
  private Double entryPrice = 0.0;

  // 포지션 여부
  private boolean isPositionOpen = false;

  /**
   * 매수 의사결정.
   *
   * @param currentPrice  현재가
   * @param currentVolume 거래량
   * @return 매수결정시 true
   */
  public boolean shouldBuy(Double currentPrice, Double currentVolume) {
    // 가격 및 거래량 큐를 업데이트
    updateWindow(priceWindow, currentPrice);
    updateWindow(volumeWindow, currentVolume);

    // 이동 평균 가격과 거래량 계산
    double averagePrice = calculateAverage(priceWindow);
    double averageVolume = calculateAverage(volumeWindow);

    // 매도 포지션 닫혀있을 때 조건 확인 후 매수
    if (!isPositionOpen) {
      // 매수 조건: 현재 가격이 평균 가격보다 높고, 현재 거래량이 평균 거래량보다 높을 때
      if (currentPrice > averagePrice && currentVolume > averageVolume) {
        // 진입 가격을 현재 가격으로 설정하고 매도 포지션 열기
        entryPrice = currentPrice;
        isPositionOpen = true;
        return true;
      }
    }

    return false;
  }

  /**
   * 매도 의사결정.
   *
   * @param currentPrice  현재가
   * @return 매도 결정시 true
   */
  public boolean shouldSell(Double currentPrice) {
    // 가격 및 거래량 큐를 업데이트
    if (priceWindow.size() < WINDOW_SIZE) {
      return false;
    }

    // 매도 포지션 열려있을 때 조건 확인 후 매도
    if (isPositionOpen) {
      // 익절 기준 금액
      double takeProfitPrice = entryPrice * (1 + profitRatio);
      // 손절 기준 금액
      double stopLossPrice = entryPrice * (1 - loseRatio);

      // 매수가가 익절 기준금액을 초과하거나 손절 기준금액을 초과할경우 매도 신호
      if (currentPrice > takeProfitPrice || currentPrice < stopLossPrice) {
        isPositionOpen = false;
        return true;
      }
    }

    return false;
  }

  /**
   * 큐 업데이트.
   *
   * @param window 창 크기
   * @param value  값
   */
  private void updateWindow(Queue<Double> window, Double value) {
    // 큐가 설정된 창 크기보다 크면 가장 오래된 값을 제거
    if (window.size() == WINDOW_SIZE) {
      window.poll();
    }
    // 새로운 값을 큐에 추가
    window.add(value);
  }

  /**
   * 평균가 계산.
   *
   * @param window 창
   * @return 평균가
   */
  private double calculateAverage(Queue<Double> window) {
    double sum = 0;
    for (Double value : window) {
      sum += value;
    }
    if (!window.isEmpty()) {
      return Math.round((sum / window.size()) * 100.0) / 100.0; // 소수점 둘째 자리까지 반올림
    } else {
      return 0;
    }
  }
}
