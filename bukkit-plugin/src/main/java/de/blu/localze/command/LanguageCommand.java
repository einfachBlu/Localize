package de.blu.localze.command;

import de.blu.localize.LocalizeAPI;
import de.blu.localize.data.Locale;
import de.blu.localize.repository.PlayerLocaleRepository;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class LanguageCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    PlayerLocaleRepository playerLocaleRepository = PlayerLocaleRepository.getInstance();
    LocalizeAPI localizeAPI = LocalizeAPI.getInstance();

    if (!(sender instanceof Player)) {
      return false;
    }

    Player player = (Player) sender;

    if (args.length == 0) {
      player.sendMessage(localizeAPI.getMessage(player.getUniqueId(), "command-language-usage"));
      return false;
    }

    String newLanguage = args[0];
    try {
      Locale locale = Locale.valueOf(newLanguage);

      localizeAPI.setLocaleAsync(
          player.getUniqueId(),
          locale,
          () -> {
            player.sendMessage(localizeAPI.getMessage(locale, "command-language-success", locale.name()));
          });
      return true;
    } catch (IllegalArgumentException e) {
      player.sendMessage(
          localizeAPI.getMessage(player.getUniqueId(), "command-language-invalid-locale"));
      return false;
    }
  }

  @Override
  public @Nullable List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    List<String> completions =
        Arrays.stream(Locale.values()).map(Locale::name).collect(Collectors.toList());

    if (args.length == 0) {
      return completions;
    }

    return completions;
  }
}
