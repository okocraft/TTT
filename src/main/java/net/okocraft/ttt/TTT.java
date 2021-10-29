package net.okocraft.ttt;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.kyori.adventure.key.Key;
import net.okocraft.ttt.bridge.worldguard.WorldGuardAPI;
import net.okocraft.ttt.bridge.worldguard.WorldGuardAPIImpl;
import net.okocraft.ttt.bridge.worldguard.WorldGuardAPIVoid;
import net.okocraft.ttt.command.TTTCommand;
import net.okocraft.ttt.config.RootSetting;
import net.okocraft.ttt.module.farm.FarmListener;
import net.okocraft.ttt.module.spawner.Spawner;
import net.okocraft.ttt.module.spawner.SpawnerListener;
import net.okocraft.ttt.database.Database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;

/**
 * 追加予定
 * * wg保護内部でスポナーのモブのみを制限するフラグ
 * * クリックボットの禁止（interacteventでやると長押しも判定されるのでダメ）
 * * mobstackerの機能
 * * スポナー使用権機能(使用権のあるプレイヤーのみ攻撃でき、ドロップアイテムを拾える)
 * 
 * 追加済み
 * * 自然に出現するスポナーのモブタイプの種類を増やす機能
 * * 自然湧きTT検知機能
 * * モブの湧き数制限
 * * 採掘可能スポナー数の制限
 * * 総湧き数制限機能
 * * silkspawnerのような機能
 * * spawner eggで中身を変えるかどうかの権限
 * * 一定範囲でのスポナー数制限機能
 * * スポナー保護機能
 * * スポナーの湧き止め機能(レッドストーンのあれ)
 */
public class TTT extends JavaPlugin {

    private final Path pluginDirectory = getDataFolder().toPath();

    private final YamlConfiguration configuration =
            YamlConfiguration.create(pluginDirectory.resolve("config.yml"));

    private final YamlConfiguration playerData =
            YamlConfiguration.create(pluginDirectory.resolve("playerdata.yml"));

    private final TranslationDirectory translationDirectory =
            TranslationDirectory.create(pluginDirectory.resolve("languages"), Key.key("ttt", "languages"));

    private RootSetting setting;

    private Database database;

    private WorldGuardAPI worldGuardAPI;

    @Override
    public void onLoad() {
        Spawner.initialize(this);

        reload();

        try {
            this.database = new Database(this);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Cound not initialize database.", e);
        }
    }

    @Override
    public void onEnable() {
        var cmd =
                Optional.ofNullable(getCommand("ttt"))
                        .orElseThrow(() -> new IllegalStateException("Could not get /ttt command"));

        TTTCommand.register(this, cmd);

        getServer().getPluginManager().registerEvents(new SpawnerListener(this), this);
        getServer().getPluginManager().registerEvents(new FarmListener(this), this);

        try {
            worldGuardAPI = new WorldGuardAPIImpl();
        } catch (NoClassDefFoundError e) {
            worldGuardAPI = new WorldGuardAPIVoid();
        }
    }

    @Override
    public void onDisable() {
        translationDirectory.unload();
        database.dispose();
    }

    public @NotNull YamlConfiguration getConfiguration() {
        return configuration;
    }

    public RootSetting getSetting() {
        return setting;
    }

    public @NotNull YamlConfiguration getPlayerData() {
        return playerData;
    }

    public @NotNull Path getPluginDirectory() {
        return pluginDirectory;
    }

    /**
     * @deprecated Use {@link #getConfiguration()}.
     */
    @Deprecated
    @Override
    public @NotNull FileConfiguration getConfig() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Use {@link com.github.siroshun09.configapi.api.file.FileConfiguration#reload()}.
     */
    @Deprecated
    @Override
    public void reloadConfig() {
        throw new UnsupportedOperationException();
    }

    private void saveDefaultLanguages(@NotNull Path directory) throws IOException {
        var japanese = "ja_JP.yml";
        ResourceUtils.copyFromJar(getFile().toPath(), "languages/" + japanese, directory.resolve(japanese));
    }

    public Database getDatabase() {
        return database;
    }

    public WorldGuardAPI getWorldGuardAPI() {
        return worldGuardAPI;
    }

    public void reload() {
        try {
            ResourceUtils.copyFromJarIfNotExists(getFile().toPath(), "config.yml", configuration.getPath());
            configuration.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml", e);
        }

        setting = RootSetting.DESERIALIZER.deserializeConfiguration(configuration);

        try {
            ResourceUtils.copyFromJarIfNotExists(getFile().toPath(), "playerdata.yml", playerData.getPath());
            playerData.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load playerdata.yml", e);
        }

        translationDirectory.getRegistry().defaultLocale(Locale.JAPAN);

        try {
            translationDirectory.createDirectoryIfNotExists(this::saveDefaultLanguages);
            translationDirectory.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load language files.", e);
        }
    }

    /**
     * Prints debug message on console.
     * 
     * @param debugMessage debug message
     */
    public static void debug(String debugMessage) {
        TTT plugin = getPlugin(TTT.class);
        if (plugin.getSetting().debug()) {
            plugin.getLogger().info("[dbg] " + debugMessage);
        }
    }
}
