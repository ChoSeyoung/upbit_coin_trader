package my.trader.coin.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 문자와 관련된 내용을 처리해주는 메서드들의 집합체입니다.
 * <a href="https://ko.wikipedia.org/wiki/%EC%84%B8%EC%A2%85">...</a>
 */
@Component
public class CharacterUtility {
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
  public static <T> String createQueryString(T dto, boolean isEncodingRequired) {
    Map<String, Object> params = new HashMap<>();

    if (dto instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (entry.getKey() instanceof String key && entry.getValue() != null) {
          String fieldName = CharacterUtility.camelToSnakeCase(key);
          processValueForCreateQueryString(params, fieldName, entry.getValue(), isEncodingRequired);
        }
      }
    } else {
      for (Field field : dto.getClass().getDeclaredFields()) {
        field.setAccessible(true);
        try {
          Object value = field.get(dto);
          if (value != null) {
            String fieldName = CharacterUtility.camelToSnakeCase(field.getName());
            processValueForCreateQueryString(params, fieldName, value, isEncodingRequired);
          }
        } catch (IllegalAccessException e) {
          System.err.println("Authorization 헤더 생성하는 중 오류가 발생: " + e.getMessage());
        }
      }
    }

    return params.entrySet()
          .stream()
          .sorted(Map.Entry.comparingByKey()) // 키를 기준으로 오름차순 정렬
          .map(entry -> entry.getKey() + "=" + entry.getValue())
          .collect(Collectors.joining("&"));
  }

  private static void processValueForCreateQueryString(Map<String, Object> params, String fieldName,
                                                       Object value, boolean isEncodingRequired) {
    if (value == null) {
      return;
    }

    if (value.getClass().isArray()) {
      processArray(params, fieldName, value, isEncodingRequired);
    } else if (value instanceof List<?>) {
      processList(params, fieldName, (List<?>) value, isEncodingRequired);
    } else {
      processSingleValue(params, fieldName, value, isEncodingRequired);
    }
  }

  private static void processArray(Map<String, Object> params, String fieldName, Object array,
                                   boolean isEncodingRequired) {
    int length = Array.getLength(array);
    for (int i = 0; i < length; i++) {
      String element = Array.get(array, i).toString();
      addParam(params, fieldName + "[]", element, isEncodingRequired);
    }
  }

  private static void processList(Map<String, Object> params, String fieldName, List<?> list,
                                  boolean isEncodingRequired) {
    for (Object listElement : list) {
      String element = listElement.toString();
      addParam(params, fieldName + "[]", element, isEncodingRequired);
    }
  }

  private static void processSingleValue(Map<String, Object> params, String fieldName, Object value,
                                         boolean isEncodingRequired) {
    String element = value.toString();
    addParam(params, fieldName, element, isEncodingRequired);
  }

  private static void addParam(Map<String, Object> params, String fieldName, String value,
                               boolean isEncodingRequired) {
    String encodedValue = isEncodingRequired ? encode(value) : value;
    params.put(fieldName, encodedValue);
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
