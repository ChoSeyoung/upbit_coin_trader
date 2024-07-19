package my.trader.coin.util;

import java.util.Random;
import java.util.UUID;

/**
 * 업비트 API 에 활용할 Identifier 를 생성해주는 메서드.
 */
public class IdentifierGenerator {

  private static final Random random = new Random();

  /**
   * user_id#tradeType#UUID 형식의 고유 식별자를 생성하는 메서드.
   *
   * @param userId    사용자 ID
   * @param tradeType 거래 타입 (ex. "buy" or "sell")
   * @return 고유 식별자
   */
  public static String generateUniqueIdentifier(Long userId, String tradeType) {
    String uuid = UUID.randomUUID().toString();
    return userId + "#" + tradeType + "#" + uuid;
  }
}
