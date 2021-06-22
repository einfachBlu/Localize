package de.blu.localize;

import de.blu.database.DatabaseAPI;
import de.blu.database.storage.cassandra.CassandraConnection;
import de.blu.database.storage.redis.RedisConnection;
import de.blu.localize.data.Locale;
import de.blu.localize.repository.PlayerLocaleRepository;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class LocalizeAPI {

  @Getter private static LocalizeAPI instance;

  private File localesBaseDirectory;
  private Map<Locale, Map<String, String>> messages = new HashMap<>();

  private ExecutorService executorService = Executors.newCachedThreadPool();

  public LocalizeAPI(File localesBaseDirectory) {
    LocalizeAPI.instance = this;
    this.localesBaseDirectory = localesBaseDirectory;
  }

  public static void init(File localesBaseDirectory) {
    LocalizeAPI localizeAPI = new LocalizeAPI(localesBaseDirectory);
    localizeAPI.loadLocales();
    PlayerLocaleRepository.init();
  }

  public void loadLocales() {
    if (!this.localesBaseDirectory.exists()) {
      this.localesBaseDirectory.mkdirs();
    }

    for (Locale locale : Locale.values()) {
      this.messages.put(locale, new HashMap<>());

      File localeDirectory = new File(this.localesBaseDirectory, locale.getLanguageCode());
      if (!localeDirectory.exists()) {
        localeDirectory.mkdirs();
        continue;
      }

      File[] files = localeDirectory.listFiles();
      if (files == null) {
        return;
      }

      for (File file : files) {
        if (!file.getName().toLowerCase().endsWith(".properties")) {
          continue;
        }

        Properties properties = new Properties();
        try (InputStreamReader inputStreamReader =
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
          properties.load(inputStreamReader);
          for (Object key : properties.keySet()) {
            this.messages.get(locale).put((String) key, properties.getProperty((String) key));
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public String getMessage(UUID playerId, String messageKey, Object... args) {
    return this.getMessage(this.getLocale(playerId), messageKey, args);
  }

  public String getMessage(Locale locale, String messageKey, Object... args) {
    String message = this.messages.get(locale).getOrDefault(messageKey, "N/A (" + messageKey + ")");

    // Replace Args
    for (int i = 0; i < args.length; i++) {
      String argumentValue = String.valueOf(args[i]);

      message = message.replaceAll("\\{" + i + "}", argumentValue);
    }

    // Replace Custom Line Seperator
    message = message.replaceAll("\\{NEXT_LINE}", "\n");

    // Replace custom locale Strings
    int count = 0;
    while (message.contains("%")) {
      boolean found = false;
      String parameter = "";

      for (int i = 0; i < message.length(); i++) {
        Character character = message.charAt(i);
        if (character.equals('%')) {
          if (found) {
            break;
          }

          found = true;
          continue;
        }

        if (found) {
          parameter += character.toString();
        }
      }

      if (this.messages.get(locale).containsKey(parameter)) {
        message = message.replaceAll("%" + parameter + "%", this.getMessage(locale, parameter));
      }

      // Prevent overflow if 2 localized messages depend on each other
      if (++count > 20) {
        break;
      }
    }

    return message;
  }

  public Locale getLocale(UUID playerId) {
    PlayerLocaleRepository playerLocaleRepository = PlayerLocaleRepository.getInstance();

    // Return cached Locale
    if (playerLocaleRepository.containsKey(playerId)) {
      return playerLocaleRepository.get(playerId);
    }

    // Get from Database and cache it
    Locale locale = Locale.ENGLISH;
    if (DatabaseAPI.getInstance().getCassandraConfig().isEnabled()) {
      CassandraConnection cassandraConnection = DatabaseAPI.getInstance().getCassandraConnection();

      if (cassandraConnection.isConnected()) {
        Map<Integer, Map<String, Object>> data =
            cassandraConnection.selectAll("player_locales", "player", playerId);

        if (data.size() > 0) {
          locale = Locale.valueOf((String) data.values().iterator().next().get("locale"));
          playerLocaleRepository.put(playerId, locale);

          if (DatabaseAPI.getInstance().getRedisConfig().isEnabled()) {
            RedisConnection redisConnection = DatabaseAPI.getInstance().getRedisConnection();

            if (redisConnection.isConnected()) {
              redisConnection.publish("LanguageUpdated", playerId.toString() + ";" + locale.name());
            }
          }
        }
      }
    }

    return locale;
  }

  public void setLocale(UUID playerId, Locale newLocale) {
    PlayerLocaleRepository playerLocaleRepository = PlayerLocaleRepository.getInstance();
    playerLocaleRepository.put(playerId, newLocale);

    if (DatabaseAPI.getInstance().getRedisConfig().isEnabled()) {
      RedisConnection redisConnection = DatabaseAPI.getInstance().getRedisConnection();

      if (redisConnection.isConnected()) {
        redisConnection.publish("LanguageUpdated", playerId.toString() + ";" + newLocale.name());
      }
    }

    if (DatabaseAPI.getInstance().getCassandraConfig().isEnabled()) {
      CassandraConnection cassandraConnection = DatabaseAPI.getInstance().getCassandraConnection();

      if (cassandraConnection.isConnected()) {
        // Set Language in Database
        cassandraConnection.selectAllAsync(
            "player_locales",
            "player",
            playerId,
            data -> {
              if (data.size() == 0) {
                // Insert
                cassandraConnection.insertIntoAsync(
                    "player_locales",
                    new String[] {"player", "locale"},
                    new Object[] {playerId, newLocale.name()});
              } else {
                // Update
                cassandraConnection.updateAsync(
                    "player_locales",
                    new String[] {"locale"},
                    new Object[] {newLocale.name()},
                    "player",
                    playerId);
              }
            });
      }
    }
  }

  public void getLocaleAsync(UUID playerId, Consumer<Locale> localeCallback) {
    this.executorService.submit(
        () -> {
          localeCallback.accept(this.getLocale(playerId));
        });
  }

  public void setLocaleAsync(UUID playerId, Locale newLocale, Runnable doneCallback) {
    this.executorService.submit(
        () -> {
          this.setLocale(playerId, newLocale);
          doneCallback.run();
        });
  }
}
