package my.trader.coin.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import my.trader.coin.dto.exchange.ClosedOrderResponseDto;
import my.trader.coin.model.ClosedOrder;
import my.trader.coin.repository.ClosedOrderReportRepository;
import my.trader.coin.repository.ClosedOrderRepository;
import org.springframework.stereotype.Service;

@Service
public class ClosedOrderService {
  private final ClosedOrderRepository closedOrderRepository;

  public ClosedOrderService(ClosedOrderRepository closedOrderRepository) {
    this.closedOrderRepository = closedOrderRepository;
  }

  @Transactional
  public void initClosedOrders() {
    closedOrderRepository.deleteAll();
  }

  @Transactional
  public void saveClosedOrder(List<ClosedOrderResponseDto> closedOrderResponseDtoList) {
    List<ClosedOrder> closedOrders = closedOrderResponseDtoList.stream()
          .map(this::convertToEntity)
          .collect(Collectors.toList());

    closedOrderRepository.saveAll(closedOrders);
  }

  private ClosedOrder convertToEntity(ClosedOrderResponseDto dto) {
    ClosedOrder closedOrder = new ClosedOrder();
    closedOrder.setUuid(dto.getUuid());
    closedOrder.setSide(dto.getSide());
    closedOrder.setOrdType(dto.getOrdType());
    closedOrder.setPrice(dto.getPrice());
    closedOrder.setState(dto.getState());
    closedOrder.setMarket(dto.getMarket());
    closedOrder.setCreatedAt(dto.getCreatedAt().plusHours(9));
    closedOrder.setVolume(dto.getVolume());
    closedOrder.setRemainingVolume(dto.getRemainingVolume());
    closedOrder.setReservedFee(dto.getReservedFee());
    closedOrder.setRemainingFee(dto.getRemainingFee());
    closedOrder.setPaidFee(dto.getPaidFee());
    closedOrder.setLocked(dto.getLocked());
    closedOrder.setExecutedVolume(dto.getExecutedVolume());
    closedOrder.setExecutedFunds(dto.getExecutedFunds());
    closedOrder.setTradesCount(dto.getTradesCount());
    closedOrder.setTimeInForce(dto.getTimeInForce());
    return closedOrder;
  }
}
