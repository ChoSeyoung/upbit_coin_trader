package my.trader.coin.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "closed_orders_report")
@Getter
@Setter
public class ClosedOrderReport {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String market;
  private LocalDate reportDate;
  private Integer reportHour;
  private Double profit;
}
