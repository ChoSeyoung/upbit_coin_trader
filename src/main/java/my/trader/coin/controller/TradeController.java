package my.trader.coin.controller;

import my.trader.coin.model.Trade;
import my.trader.coin.service.TradeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/trades")
    public String getTrades(Model model) {
        List<Trade> trades = tradeService.getAllTrades();
        model.addAttribute("trades", trades);
        model.addAttribute("profit", tradeService.calculateProfit());
        return "trades";
    }

}
