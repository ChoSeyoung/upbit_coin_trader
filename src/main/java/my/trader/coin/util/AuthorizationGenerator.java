package my.trader.coin.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 업비트 API 호출 시 Authorization 값을 생성해주는 메서드.
 */
@Component
public class AuthorizationGenerator {
  private static final Logger logger = LoggerFactory.getLogger(AuthorizationGenerator.class);

  /**
   * 파라미터가 없을 경우 Authorization 값 조회.
   *
   * @return Bearer ${Authorization}
   */
  public String generateTokenWithoutParameter() {
    Algorithm algorithm = Algorithm.HMAC256(System.getProperty("secret"));

    String jwtToken = JWT.create()
          .withClaim("access_key", System.getProperty("access"))
          .withClaim("nonce", UUID.randomUUID().toString())
          .sign(algorithm);

    return "Bearer " + jwtToken;
  }

  /**
   * 파라미터가 있을 경우 Authorization 값 조회.
   *
   * @return Bearer ${Authorization}
   */
  public <T> String generateTokenWithParameter(T dto) {
    String jwtToken = null;

    try {
      String queryString = CharacterUtility.createQueryString(dto, false);

      MessageDigest md = MessageDigest.getInstance("SHA-512");
      md.update(queryString.getBytes(StandardCharsets.UTF_8));

      byte[] digest = md.digest();
      String queryHash = String.format("%0128x", new BigInteger(1, digest));

      Algorithm algorithm = Algorithm.HMAC256(System.getProperty("secret"));
      jwtToken = JWT.create()
            .withClaim("access_key", System.getProperty("access"))
            .withClaim("nonce", UUID.randomUUID().toString())
            .withClaim("query_hash", queryHash)
            .withClaim("query_hash_alg", "SHA512")
            .sign(algorithm);

      jwtToken = "Bearer " + jwtToken;
    } catch (NoSuchAlgorithmException e) {
      logger.error("암호화 알고리즘을 찾을 수 없는 오류 발생", e);
    }

    return jwtToken;
  }
}
