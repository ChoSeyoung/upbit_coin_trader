package my.trader.coin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * [users] 테이블 Entity.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "json")
  private Map<String, Double> inventory = new HashMap<>();

  /**
   * 티커심볼을 이용한 보유량 조회.
   *
   * @param symbol 티커심볼
   * @return 보유량
   */
  public double getInventory(String symbol) {
    if (inventory == null) {
      inventory = new HashMap<>();
    }
    return inventory.getOrDefault(symbol, 0.0);
  }

  /**
   * 티커심볼을 이용한 보유량 업데이트.
   *
   * @param symbol   티커심볼
   * @param quantity 업데이트 될 수량
   */
  public void updateInventory(String symbol, double quantity) {
    if (inventory == null) {
      inventory = new HashMap<>();
    }
    inventory.put(symbol, quantity);
  }
}
