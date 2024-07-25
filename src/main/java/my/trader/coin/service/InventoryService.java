package my.trader.coin.service;

import my.trader.coin.enums.TradeType;
import my.trader.coin.model.Inventory;
import my.trader.coin.repository.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
  private final InventoryRepository inventoryRepository;

  public InventoryService(InventoryRepository inventoryRepository) {
    this.inventoryRepository = inventoryRepository;
  }

  public void saveQuantity(TradeType tradeType, String market, Double quantity) {
    Inventory inventory = inventoryRepository.findById(market).orElse(new Inventory());
    inventory.setMarket(market);

    double currentQuantity = inventory.getQuantity() != null ? inventory.getQuantity() : 0.0;
    if (tradeType.isBuy()) {
      currentQuantity += quantity;
    } else if (tradeType.isSell()) {
      currentQuantity -= quantity;
    }
    inventory.setQuantity(currentQuantity);

    inventoryRepository.save(inventory);
  }

  public Double getQuantityByMarket(String market) {
    return inventoryRepository.findQuantityByMarket(market);
  }
}
