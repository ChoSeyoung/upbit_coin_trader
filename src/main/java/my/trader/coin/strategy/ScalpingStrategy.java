package my.trader.coin.strategy;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Queue;

@Service
public class ScalpingStrategy {

    // 가격 이동 평균을 계산할 창 크기
    private static final int WINDOW_SIZE = 5;

    // 최근 가격을 저장하는 큐
    private final Queue<BigDecimal> priceWindow = new LinkedList<>();

    // 최근 거래량을 저장하는 큐
    private final Queue<BigDecimal> volumeWindow = new LinkedList<>();

    // 손절매 및 목표 이익 비율을 설정 (수정 불가)
    private static final BigDecimal STOP_LOSS_PERCENTAGE = BigDecimal.valueOf(0.015); // 2% 손절매
    private static final BigDecimal TAKE_PROFIT_PERCENTAGE = BigDecimal.valueOf(0.015); // 2% 목표 이익
    private static final BigDecimal EXCHANGE_FEE_PERCENTAGE = BigDecimal.valueOf(0.0005); // 거래수수료

    // 진입 가격
    private BigDecimal entryPrice = new BigDecimal(0);

    // 포지션 여부
    private boolean isPositionOpen = false;

    // 매수 결정을 내리는 메서드
    public boolean shouldBuy(BigDecimal currentPrice, BigDecimal currentVolume) {
        // 가격 및 거래량 큐를 업데이트
        updateWindow(priceWindow, currentPrice);
        updateWindow(volumeWindow, currentVolume);

        // 이동 평균 가격과 거래량 계산
        BigDecimal averagePrice = calculateAverage(priceWindow);
        BigDecimal averageVolume = calculateAverage(volumeWindow);

        // 매수 조건: 현재 가격이 평균 가격보다 높고, 현재 거래량이 평균 거래량보다 높을 때
        if (currentPrice.compareTo(averagePrice) > 0 && currentVolume.compareTo(averageVolume) > 0) {
            // 진입 가격을 현재 가격으로 설정하고 포지션 열기
            entryPrice = currentPrice;
            isPositionOpen = true;
            return true;
        }

        return false;
    }

    // 매도 결정을 내리는 메서드
    public boolean shouldSell(BigDecimal currentPrice, BigDecimal currentVolume) {
        if (priceWindow.size() < WINDOW_SIZE) {
            return false;
        }

        BigDecimal averagePrice = calculateAverage(priceWindow);
        BigDecimal averageVolume = calculateAverage(volumeWindow);

        if (isPositionOpen) {
            BigDecimal takeProfitPrice = entryPrice.multiply(BigDecimal.ONE.add(TAKE_PROFIT_PERCENTAGE));
            BigDecimal stopLossPrice = entryPrice.multiply(BigDecimal.ONE.subtract(STOP_LOSS_PERCENTAGE));

            if (currentPrice.compareTo(takeProfitPrice) >= 0 || currentPrice.compareTo(stopLossPrice) <= 0) {
                isPositionOpen = false;
                return true;
            }
        }

        return currentPrice.compareTo(averagePrice) < 0 && currentVolume.compareTo(averageVolume) > 0;
    }

    // 큐를 업데이트하는 메서드
    private void updateWindow(Queue<BigDecimal> window, BigDecimal value) {
        // 큐가 설정된 창 크기보다 크면 가장 오래된 값을 제거
        if (window.size() == WINDOW_SIZE) {
            window.poll();
        }
        // 새로운 값을 큐에 추가
        window.add(value);
    }

    private BigDecimal calculateAverage(Queue<BigDecimal> window) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : window) {
            sum = sum.add(value);
        }
        if (!window.isEmpty()) {
            return sum.divide(BigDecimal.valueOf(window.size()), 2, RoundingMode.HALF_UP); // 2 decimal places, rounding mode HALF_UP
        } else {
            return BigDecimal.ZERO;
        }
    }
}
