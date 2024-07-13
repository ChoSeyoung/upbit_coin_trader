package my.trader.coin.service;

import my.trader.coin.enums.TradeType;
import my.trader.coin.model.Trade;
import my.trader.coin.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TradeService {
    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    public BigDecimal calculateProfit() {
        List<Trade> trades = tradeRepository.findAll();
        BigDecimal profit = BigDecimal.ZERO;

        for (Trade trade : trades) {
            BigDecimal tradeValue = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
            if (trade.getType().equals(TradeType.BUY.getName())) {
                profit = profit.subtract(tradeValue);
            } else if (trade.getType().equals(TradeType.SELL.getName())) {
                profit = profit.add(tradeValue);
            }
        }

        return profit;
    }
}
