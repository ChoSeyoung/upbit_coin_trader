package my.trader.coin.strategy;

import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;

@Service
public class ScalpingStrategy {

    private static final int WINDOW_SIZE = 5;
    private final Queue<Double> priceWindow = new LinkedList<>();

    public boolean shouldBuy(double currentPrice) {
        if (priceWindow.size() == WINDOW_SIZE) {
            priceWindow.poll();
        }
        priceWindow.add(currentPrice);

        double averagePrice = priceWindow.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return currentPrice > averagePrice;
    }

    public boolean shouldSell(double currentPrice) {
        if (priceWindow.size() < WINDOW_SIZE) {
            return false;
        }

        double averagePrice = priceWindow.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return currentPrice < averagePrice;
    }
}
