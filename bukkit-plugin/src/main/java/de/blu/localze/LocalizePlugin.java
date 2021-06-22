package de.blu.localze;

import de.blu.database.DatabaseAPI;
import de.blu.database.data.TableColumn;
import de.blu.database.data.TableColumnType;
import de.blu.database.storage.cassandra.CassandraConnection;
import de.blu.localize.LocalizeAPI;
import de.blu.localze.command.LanguageCommand;
import de.blu.localze.listener.PreloadPlayerLocales;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Singleton;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@Singleton
public final class LocalizePlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    LocalizeAPI.init(new File(this.getDataFolder(), "locales"));

    // Create Database Table if not exist
    if (DatabaseAPI.getInstance().getCassandraConfig().isEnabled()) {
      CassandraConnection cassandraConnection = DatabaseAPI.getInstance().getCassandraConnection();

      if (cassandraConnection.isConnected()) {
        List<TableColumn> columns =
            Arrays.asList(
                new TableColumn(TableColumnType.UUID, "player", true),
                new TableColumn(TableColumnType.STRING, "locale", false));

        cassandraConnection.createTableIfNotExist("player_locales", columns);
      }
    }

    // Register Listener
    Bukkit.getPluginManager().registerEvents(new PreloadPlayerLocales(), this);

    // Register Language Command
    LanguageCommand languageCommand = new LanguageCommand();
    PluginCommand command = this.getCommand("language");

    command.setExecutor(languageCommand);
    command.setTabCompleter(languageCommand);
  }
}
