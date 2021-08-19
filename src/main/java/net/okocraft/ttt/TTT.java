package net.okocraft.ttt;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.kyori.adventure.key.Key;
import net.okocraft.ttt.command.TTTCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.config.Config;

import java.util.Optional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Level;

/**
 * 追加予定
 * * 自然湧きTT検知機能
 * * silkspawnerのような機能
 * * mobstackerの機能
 * * 一定範囲でのスポナー数制限機能
 * * スポナー保護機能
 * * スポナー使用権機能(使用権のあるプレイヤーのみ攻撃でき、ドロップアイテムを拾える)
 * * 総湧き数制限機能
 * * スポナーの湧き止め機能(レッドストーンのあれ)
 * * wg保護内部でスポナーのモブのみを制限するフラグ
 * * モブの湧き数制限
 * * クリックボットの禁止（interacteventでやると長押しも判定されるのでダメ）
 * * spawner eggで中身を変えるかどうかの権限
 */
public class TTT extends JavaPlugin implements Listener {

    private final TranslationDirectory translationDirectory =
            new TranslationDirectory(getDataFolder().toPath().resolve("languages"), Key.key("ttt", "languages"));
    private Config config;

    @Override
    public void onLoad() {
        translationDirectory.getRegistry().defaultLocale(Locale.JAPAN);

        try {
            translationDirectory.createDirectoryIfNotExists(this::saveDefaultLanguages);
            translationDirectory.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load language files.", e);
        }
    }

    @Override
    public void onEnable() {
        this.config = new Config(this);

        var cmd =
                Optional.ofNullable(getCommand("ttt"))
                        .orElseThrow(() -> new IllegalStateException("Could not get /ttt command"));

        TTTCommand.register(this, cmd);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        translationDirectory.unload();
    }

    public Config getMainConfig() {
        return this.config;
    }

    /**
     * @deprecated Use {@link #getMainConfig()}.
     */
    @Deprecated
    @Override
    public FileConfiguration getConfig() {
        return config.get();
    }

    @Override
    public void reloadConfig() {
        config.reload();
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        return true;
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob)) {
            return;
        }
        if (event.getEntity().getLastDamageCause().getCause() == DamageCause.CRAMMING) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }

    private void saveDefaultLanguages(@NotNull Path directory) throws IOException {
        var japanese = "ja_JP.yml";
        ResourceUtils.copyFromJar(getFile().toPath(), "ja_JP.yml", directory.resolve(japanese));
    }
}
