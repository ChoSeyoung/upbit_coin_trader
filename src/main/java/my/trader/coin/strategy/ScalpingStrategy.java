package my.trader.coin.strategy;

import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;

@Service
public class ScalpingStrategy {

    // 가격 이동 평균을 계산할 창 크기
    private static final int WINDOW_SIZE = 5;

    // 최근 가격을 저장하는 큐
    private final Queue<Double> priceWindow = new LinkedList<>();

    // 최근 거래량을 저장하는 큐
    private final Queue<Double> volumeWindow = new LinkedList<>();

    // 손절매 및 목표 이익 비율을 설정 (수정 불가)
    private static final double STOP_LOSS_PERCENTAGE = 0.02; // 2% 손절매
    private static final double TAKE_PROFIT_PERCENTAGE = 0.02; // 2% 목표 이익

    // 진입 가격
    private double entryPrice = 0.0;

    // 포지션 여부
    private boolean isPositionOpen = false;

    // 매수 결정을 내리는 메서드
    public boolean shouldBuy(double currentPrice, double currentVolume) {
        // 가격 및 거래량 큐를 업데이트
        updateWindow(priceWindow, currentPrice);
        updateWindow(volumeWindow, currentVolume);

        // 이동 평균 가격과 거래량 계산
        double averagePrice = priceWindow.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double averageVolume = volumeWindow.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // 매수 조건: 현재 가격이 평균 가격보다 높고, 현재 거래량이 평균 거래량보다 높을 때
        if (currentPrice > averagePrice && currentVolume > averageVolume) {
            // 진입 가격을 현재 가격으로 설정하고 포지션 열기
            entryPrice = currentPrice;
            isPositionOpen = true;
            return true;
        }

        return false;
    }

    // 매도 결정을 내리는 메서드
    public boolean shouldSell(double currentPrice, double currentVolume) {
        // 이동 평균을 계산할 충분한 데이터가 없는 경우 매도 신호를 반환하지 않음
        if (priceWindow.size() < WINDOW_SIZE) {
            return false;
        }

        // 이동 평균 가격과 거래량 계산
        double averagePrice = priceWindow.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double averageVolume = volumeWindow.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // 포지션이 열려 있을 때
        if (isPositionOpen) {
            // 목표 이익에 도달했는지 확인
            if (currentPrice >= entryPrice * (1 + TAKE_PROFIT_PERCENTAGE)) {
                isPositionOpen = false; // 포지션 닫기
                return true;
            }

            // 손절매에 도달했는지 확인
            if (currentPrice <= entryPrice * (1 - STOP_LOSS_PERCENTAGE)) {
                isPositionOpen = false; // 포지션 닫기
                return true;
            }
        }

        // 기본 매도 조건: 현재 가격이 평균 가격보다 낮고, 현재 거래량이 평균 거래량보다 높을 때
        return currentPrice < averagePrice && currentVolume > averageVolume;
    }

    // 큐를 업데이트하는 메서드
    private void updateWindow(Queue<Double> window, double value) {
        // 큐가 설정된 창 크기보다 크면 가장 오래된 값을 제거
        if (window.size() == WINDOW_SIZE) {
            window.poll();
        }
        // 새로운 값을 큐에 추가
        window.add(value);
    }
}
