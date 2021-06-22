package de.blu.localize.repository;

import de.blu.database.DatabaseAPI;
import de.blu.database.storage.redis.RedisConnection;
import de.blu.localize.data.Locale;
import lombok.Getter;

import java.util.UUID;

public final class PlayerLocaleRepository extends MapRepository<UUID, Locale> {

  @Getter private static PlayerLocaleRepository instance;

  public PlayerLocaleRepository() {
    PlayerLocaleRepository.instance = this;
  }

  public static void init() {
    PlayerLocaleRepository playerLocaleRepository = new PlayerLocaleRepository();

    if (!DatabaseAPI.getInstance().getRedisConfig().isEnabled()) {
      return;
    }

    RedisConnection redisConnection = DatabaseAPI.getInstance().getRedisConnection();
    if (!redisConnection.isConnected()) {
      return;
    }

    redisConnection.subscribe(
        (channel, message) -> {
          String[] split = message.split(";");
          UUID playerId = UUID.fromString(split[0]);
          Locale locale = Locale.valueOf(split[1]);

          playerLocaleRepository.put(playerId, locale);
        },
        "LanguageUpdated");
  }
}
