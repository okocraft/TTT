package net.okocraft.ttt;

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

    private Config config;

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
}
