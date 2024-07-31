package my.trader.coin.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "configs")
@Getter
@Setter
public class Config {
  @Id
  private String name;
  @Column(length = 4000)
  private String val;
}
