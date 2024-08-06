package my.trader.coin.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "closed_orders_report")
@Getter
@Setter
public class ClosedOrderReport {
  @Id
  @Column(name = "market", nullable = false)
  private String market;
  private BigDecimal amount = BigDecimal.ZERO;

  @PrePersist
  @PreUpdate
  private void prePersistOrUpdate() {
    if (amount == null) {
      amount = BigDecimal.ZERO;
    }
  }
}
