package my.trader.coin.controller;

import my.trader.coin.service.TradeService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/trades")
    public Map<String, Object> getTrades(Model model) {
        Map<String, Object> result = new HashMap<>();

        result.put("trades", tradeService.getAllTrades());
        result.put("profit", tradeService.calculateProfit());

        return result;
    }

}
