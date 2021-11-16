package net.okocraft.ttt.module.farm;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.worldsetting.farm.FarmAction;
import net.okocraft.ttt.config.worldsetting.farm.FarmSetting;
import net.okocraft.ttt.config.worldsetting.farm.FinderSetting;
import net.okocraft.ttt.language.Messages;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        FarmSetting farmSetting = plugin.getSetting().worldSetting(entity.getWorld()).farmSetting();

        if (farmSetting.preventCrammingDeathDrop() && entity.getLastDamageCause().getCause() == DamageCause.CRAMMING) {
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

        FinderSetting finderSetting = farmSetting.finderSetting();

        Collection<FarmAction> farmActions = finderSetting.farmActions().get(spawnReason);
        int killingChumberRange = finderSetting.killingChumberRange();
        
        Condition condition = new Condition(Field.SPAWN_REASON, spawnReason)
                .and(Field.DEATH_WORLD_NAME, entity.getWorld())
                .and(Field.DEATH_X_LOCATION, deathLocation.getBlockX() - killingChumberRange, deathLocation.getBlockX() + killingChumberRange)
                .and(Field.DEATH_Z_LOCATION, deathLocation.getBlockZ() - killingChumberRange, deathLocation.getBlockZ() + killingChumberRange)
                .and(Field.DEATH_Y_LOCATION, deathLocation.getBlockY() - killingChumberRange, deathLocation.getBlockY() + killingChumberRange);
        
        List<LogEntity> searchResults = dataTable.search(condition);
        
        if (searchResults.size() >= finderSetting.killedMobsToBeKillingChumber()) {
            for (FarmAction action : farmActions) {
                if (action == FarmAction.CLEAR_DROP) {
                    event.getDrops().clear();
                }
                if (action == FarmAction.CLEAR_EXP) {
                    event.setDroppedExp(0);
                }
                if (action == FarmAction.NOTIFY) {
                    Component farmIsDetectedMessage = Messages.FARM_IS_DETECTED.apply(log);
                    plugin.getDiscord().send(
                            PlainTextComponentSerializer.plainText().serialize(
                                    GlobalTranslator.render(farmIsDetectedMessage, Locale.getDefault())
                            )
                    );
                    WrappedLocation deathLoc = WrappedLocation.of(log.getDeathLocation());
                    Bukkit.getOnlinePlayers().forEach(player -> {                        
                        if (player.hasPermission("ttt.notification.farmfound") && !farmLocationsDetected.containsKey(player.getUniqueId())) {
                            farmLocationsDetected.put(player.getUniqueId(), deathLoc);
                            player.sendMessage(farmIsDetectedMessage);
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
