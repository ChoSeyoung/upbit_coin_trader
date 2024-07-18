package my.trader.coin.enums;

import lombok.Getter;

/**
 * 콘솔에 출력할 때 텍스트의 색상을 변경하는 데 사용할 ANSI 컬러 코드 집합체입니다.
 */
@Getter
public enum ColorfulConsoleOutput {
  RESET("\u001B[0m"),
  BLACK("\u001B[30m"),
  RED("\u001B[31m"),
  GREEN("\u001B[32m"),
  YELLOW("\u001B[33m"),
  BLUE("\u001B[34m"),
  PURPLE("\u001B[35m"),
  CYAN("\u001B[36m"),
  WHITE("\u001B[37m");

  private final String code;

  ColorfulConsoleOutput(String code) {
    this.code = code;
  }

  /**
   * ANSI 컬러 코드를 이용하여 콘솔에 출력합니다.
   *
   * @param message 텍스트
   * @param color   ANSI 컬러
   */
  public static void printWithColor(String message, ColorfulConsoleOutput color) {
    System.out.println(color.getCode() + message + ColorfulConsoleOutput.RESET.getCode());
  }
}

