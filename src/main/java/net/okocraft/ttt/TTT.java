package net.okocraft.ttt;

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

public class TTT extends JavaPlugin implements Listener {

    private Config config;

    @Override
    public void onEnable() {
        this.config = new Config(this);

        getCommand("ttt").setExecutor(this);

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
