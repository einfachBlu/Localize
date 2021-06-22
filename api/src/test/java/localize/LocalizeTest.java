package localize;

import de.blu.localize.LocalizeAPI;
import de.blu.localize.converter.DecimalNumber;
import de.blu.localize.converter.PeriodNumber;
import de.blu.localize.data.Locale;

import java.io.File;

public final class LocalizeTest {
  public static void main(String[] args) {
    LocalizeAPI.init(new File("C:\\Users/Blu/IdeaProjects/localize/api/build/data/locales"));
    LocalizeAPI localizeAPI = LocalizeAPI.getInstance();

    System.out.println(localizeAPI.getMessage(Locale.GERMAN, "test-helloworld", new PeriodNumber(75000)));
    System.out.println(localizeAPI.getMessage(Locale.GERMAN, "test-helloworld", new DecimalNumber(150.2873, 2, true))); // Should be 150.29
    System.out.println(localizeAPI.getMessage(Locale.GERMAN, "test-helloworld", new DecimalNumber(150.2873, 2, false))); // Should be 150.28
    System.out.println(localizeAPI.getMessage(Locale.GERMAN, "test-helloworld", new DecimalNumber(150.2873, 0, true))); // Should be 150
    System.out.println(localizeAPI.getMessage(Locale.GERMAN, "test-helloworld", new DecimalNumber(150.2873, 3, false))); // Should be 150.287
    System.out.println(localizeAPI.getMessage(Locale.GERMAN, "test-helloworld", new DecimalNumber(150.7873, 0, true))); // Should be 151
    System.out.println(localizeAPI.getMessage(Locale.GERMAN, "test-helloworld", new DecimalNumber(150.7873, 0, false))); // Should be 150
  }
}
