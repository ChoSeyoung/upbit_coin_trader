package my.trader.coin.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 업비트 API 호출 시 Authorization 값을 생성해주는 메서드.
 */
@Component
public class AuthorizationGenerator {
  private static final Logger logger = LoggerFactory.getLogger(AuthorizationGenerator.class);

  // API KEY
  @Value("${upbit.api.key}")
  private String apiKey;

  // API SECRET KEY
  @Value("${upbit.secret.key}")
  private String secretKey;

  /**
   * 파라미터가 없을 경우 Authorization 값 조회.
   *
   * @return Bearer ${Authorization}
   */
  public String generateTokenWithoutParameter() {
    Algorithm algorithm = Algorithm.HMAC256(secretKey);

    String jwtToken = JWT.create()
          .withClaim("access_key", apiKey)
          .withClaim("nonce", UUID.randomUUID().toString())
          .sign(algorithm);

    return "Bearer " + jwtToken;
  }

  /**
   * 라미터가 있을 경우 Authorization 값 조회.
   *
   * @return Bearer ${Authorization}
   */
  public <T> String generateTokenWithParameter(T dto) {
    String jwtToken = null;

    try {
      String queryString = createQueryString(dto);

      MessageDigest md = MessageDigest.getInstance("SHA-512");
      md.update(queryString.getBytes(StandardCharsets.UTF_8));

      String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

      Algorithm algorithm = Algorithm.HMAC256(secretKey);
      jwtToken = JWT.create()
            .withClaim("access_key", apiKey)
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

  /**
   * DTO 객체를 쿼리 스트링으로 변환.
   *
   * @param dto DTO 객체
   * @return 쿼리 스트링
   */
  private <T> String createQueryString(T dto) {
    Map<String, String> params = new HashMap<>();

    for (Field field : dto.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      try {
        Object value = field.get(dto);
        if (value != null) {
          if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
              Object arrayElement = Array.get(value, i);
              params.put(field.getName() + "[]", arrayElement.toString());
            }
          } else if (value instanceof List<?> list) {
            for (Object listElement : list) {
              params.put(field.getName() + "[]", listElement.toString());
            }
          } else {
            params.put(field.getName(), value.toString());
          }
        }
      } catch (IllegalAccessException e) {
        logger.error("Authorization 헤더 생성하는 중 오류가 발생", e);
      }
    }

    return params.entrySet()
          .stream()
          .map(entry -> entry.getKey() + "=" + entry.getValue())
          .collect(Collectors.joining("&"));
  }
}
