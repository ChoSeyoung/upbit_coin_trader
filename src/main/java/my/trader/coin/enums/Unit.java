package my.trader.coin.enums;

import lombok.Getter;

/**
 * 업비트 정책
 * 캔들 단위 x분 에 대한 정의.
 */
@Getter
public enum Unit {
  UNIT_1(1),
  UNIT_3(3),
  UNIT_5(5),
  UNIT_15(15),
  UNIT_10(10),
  UNIT_30(30),
  UNIT_60(60),
  UNIT_240(240);

  private int unit;

  Unit(int unit) {
    this.unit = unit;
  }
}
