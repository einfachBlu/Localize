package de.blu.localize.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum Locale {
  GERMAN("de_DE"),
  ENGLISH("en_EN");

  private String languageCode;

  public static Locale byLanguageCode(String languageCode) {
    return Arrays.stream(Locale.values())
        .filter(locale -> locale.getLanguageCode().equalsIgnoreCase(languageCode))
        .findFirst()
        .orElse(null);
  }
}
