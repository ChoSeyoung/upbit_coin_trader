package my.trader.coin.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
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
    private double price;
    private double quantity;
    private LocalDateTime timestamp;

    // getters and setters
}
