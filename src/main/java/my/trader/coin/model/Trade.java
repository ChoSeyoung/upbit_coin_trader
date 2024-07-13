package my.trader.coin.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Getter
@Setter
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // BUY or SELL
    private BigDecimal price;
    private Double quantity;
    private LocalDateTime timestamp;
}
