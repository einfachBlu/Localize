package de.blu.localize.converter;

import lombok.AllArgsConstructor;

import java.text.NumberFormat;
import java.util.Locale;

@AllArgsConstructor
public final class PeriodNumber {

  private long value;

  @Override
  public String toString() {
    return NumberFormat.getIntegerInstance(Locale.GERMANY).format(this.value);
  }
}
