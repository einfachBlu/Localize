package de.blu.localize.converter;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@AllArgsConstructor
public final class DecimalNumber {

  private double value;
  private int decimalPlaces;
  private boolean round;

  @Override
  public String toString() {
    BigDecimal bigDecimal = new BigDecimal(this.value);
    bigDecimal = bigDecimal.setScale(this.decimalPlaces, this.round ? RoundingMode.HALF_UP : RoundingMode.DOWN);

    return bigDecimal.toString();
  }
}
