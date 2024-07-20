package my.trader.coin.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 우리의 세종은 문자와 관련된 내용을 처리해주는 메서드들의 집합체입니다.
 * <a href="https://ko.wikipedia.org/wiki/%EC%84%B8%EC%A2%85">...</a>
 */
public class Sejong {
  /**
   * 카멜 케이스를 스네이크 케이스로 변환.
   *
   * @param camelCase 카멜 케이스 문자열
   * @return 스네이크 케이스 문자열
   */
  public static String camelToSnakeCase(String camelCase) {
    StringBuilder result = new StringBuilder();
    for (char character : camelCase.toCharArray()) {
      if (Character.isUpperCase(character)) {
        result.append("_").append(Character.toLowerCase(character));
      } else {
        result.append(character);
      }
    }
    return result.toString();
  }

  /**
   * DTO 객체를 쿼리 스트링으로 변환.
   *
   * @param dto DTO 객체
   * @return 쿼리 스트링
   */
  public static <T> String createQueryString(T dto) {
    Map<String, String> params = new HashMap<>();

    for (Field field : dto.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      try {
        Object value = field.get(dto);
        if (value != null) {
          String fieldName = Sejong.camelToSnakeCase(field.getName());
          if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
              Object arrayElement = Array.get(value, i);
              params.put(fieldName + "[]", arrayElement.toString());
            }
          } else if (value instanceof List<?> list) {
            for (Object listElement : list) {
              params.put(fieldName + "[]", listElement.toString());
            }
          } else {
            params.put(fieldName, value.toString());
          }
        }
      } catch (IllegalAccessException e) {
        System.err.println("Authorization 헤더 생성하는 중 오류가 발생: " + e.getMessage());
      }
    }

    return params.entrySet()
          .stream()
          .map(entry -> entry.getKey() + "=" + entry.getValue())
          .collect(Collectors.joining("&"));
  }
}
