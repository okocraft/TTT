package net.okocraft.ttt.module.farm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.siroshun09.configapi.api.Configuration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.Messages;
import net.okocraft.ttt.config.Settings;
import net.okocraft.ttt.config.enums.FindFarmsAction;
import net.okocraft.ttt.module.farm.EntityDeathLogTable.Condition;
import net.okocraft.ttt.module.farm.EntityDeathLogTable.Field;
import net.okocraft.ttt.module.farm.EntityDeathLogTable.LogEntity;

public class FarmListener implements Listener {

    private final TTT plugin;
    private final EntityDeathLogTable dataTable;
    private final Map<UUID, WrappedLocation> farmLocationsDetected = new HashMap<>();
    
    public FarmListener(TTT plugin) {
        this.plugin = plugin;
        this.dataTable = new EntityDeathLogTable(plugin.getDatabase());
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getLastDamageCause().getCause() == DamageCause.CRAMMING) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }

        SpawnReason spawnReason = entity.getEntitySpawnReason();
        if (spawnReason == null) {
            return;
        }

        Location spawnLocation = entity.getOrigin();
        if (spawnLocation == null) {
            return;
        }
        Location deathLocation = entity.getLocation();
        LogEntity log = new LogEntity(
                System.currentTimeMillis(),
                (int) (System.nanoTime() % 1000000000),
                event.getEntityType(),
                spawnReason,
                spawnLocation.getWorld().getName(),
                spawnLocation.getBlockX(),
                spawnLocation.getBlockY(),
                spawnLocation.getBlockZ(),
                deathLocation.getWorld().getName(),
                deathLocation.getBlockX(),
                deathLocation.getBlockY(),
                deathLocation.getBlockZ()
        );

        Configuration config = plugin.getConfiguration();
        List<FindFarmsAction> farmActions = config.get(Settings.FIND_FARMS_FARM_ACTIONS)
                .getOrDefault(spawnReason, new ArrayList<>());
        int killingChumberRange = config.get(Settings.FIND_FARMS_KILLING_CHUMBER_RANGE);
        Condition condition = new Condition(Field.SPAWN_REASON, spawnReason)
                .and(Field.DEATH_WORLD_NAME, entity.getWorld())
                .and(Field.DEATH_X_LOCATION, deathLocation.getBlockX() - killingChumberRange, deathLocation.getBlockX() + killingChumberRange)
                .and(Field.DEATH_Z_LOCATION, deathLocation.getBlockZ() - killingChumberRange, deathLocation.getBlockZ() + killingChumberRange)
                .and(Field.DEATH_Y_LOCATION, deathLocation.getBlockY() - killingChumberRange, deathLocation.getBlockY() + killingChumberRange);
        
        List<LogEntity> searchResults = dataTable.search(condition);
        
        if (searchResults.size() >= config.get(Settings.FIND_FARMS_KILLED_MOBS_TO_BE_KILLING_CHUMBER)) {
            for (FindFarmsAction action : farmActions) {
                if (action == FindFarmsAction.CLEAR_DROP) {
                    event.getDrops().clear();
                }
                if (action == FindFarmsAction.CLEAR_EXP) {
                    event.setDroppedExp(0);
                }
                if (action == FindFarmsAction.NOTIFY) {
                    WrappedLocation deathLoc = WrappedLocation.of(log.getDeathLocation());
                    Bukkit.getOnlinePlayers().forEach(player -> {                        
                        if (player.hasPermission("ttt.notification.farmfound") && !farmLocationsDetected.containsKey(player.getUniqueId())) {
                            farmLocationsDetected.put(player.getUniqueId(), deathLoc);
                            player.sendMessage(Messages.FARM_IS_DETECTED.apply(log));
                        }
                    });
                }
            }
        } else {
            new BukkitRunnable(){
                public void run() { dataTable.insert(log); }
            }.runTaskAsynchronously(plugin);
        }
    }

    private record WrappedLocation(String world, int x, int y, int z) {
        public static WrappedLocation of(Location loc) {
            return new WrappedLocation(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }
}
