package my.trader.coin.repository;

import java.util.Optional;
import my.trader.coin.model.Config;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Config, String> {
}
