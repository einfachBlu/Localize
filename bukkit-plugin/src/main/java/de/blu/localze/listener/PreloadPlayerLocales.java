package de.blu.localze.listener;

import de.blu.localize.LocalizeAPI;
import de.blu.localize.repository.PlayerLocaleRepository;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PreloadPlayerLocales implements Listener {

  private ExecutorService executorService = Executors.newCachedThreadPool();

  @EventHandler
  public void onPreLogin(AsyncPlayerPreLoginEvent e) {
    UUID playerId = e.getUniqueId();

    PlayerLocaleRepository playerLocaleRepository = PlayerLocaleRepository.getInstance();
    if (playerLocaleRepository.containsKey(playerId)) {
      return;
    }

    this.executorService.submit(
        () -> {
          LocalizeAPI.getInstance().getLocale(playerId);
        });
  }
}
