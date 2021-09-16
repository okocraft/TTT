package net.okocraft.ttt.module.farm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.module.farm.EntityDeathLogTable.Condition;
import net.okocraft.ttt.module.farm.EntityDeathLogTable.Field;
import net.okocraft.ttt.module.farm.EntityDeathLogTable.LogEntity;

public class FarmListener implements Listener {

    private final TTT plugin;
    private final EntityDeathLogTable dataTable;
    
    public FarmListener(TTT plugin) {
        this.plugin = plugin;
        this.dataTable = new EntityDeathLogTable(plugin);
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Optional<Location> optionalSpawnLocation = getSpawnLocation(entity);
        if (optionalSpawnLocation.isEmpty()) {
            return;
        }
        Location spawnLocation = optionalSpawnLocation.get();
        Location deathLocation = entity.getLocation();
        LogEntity log = new LogEntity(
                System.nanoTime(),
                event.getEntityType(),
                entity.getEntitySpawnReason(),
                spawnLocation.getWorld().getName(),
                spawnLocation.getBlockX(),
                spawnLocation.getBlockY(),
                spawnLocation.getBlockZ(),
                deathLocation.getWorld().getName(),
                deathLocation.getBlockX(),
                deathLocation.getBlockY(),
                deathLocation.getBlockZ()
        );

        dataTable.insert(log);
        dataTable.search(new Condition(Field.ENTITY, EntityType.ZOMBIE));

        for (FarmAction action : checkFarm(event)) {
            if (action == FarmAction.NOTIFY) {

            } else if (action == FarmAction.CLEAR_DROP) {
            } else if (action == FarmAction.CLEAR_DROP) {
                
            }
        }

        if (!checkFarm(event).isEmpty()) {
            
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }

    public enum FarmAction {
        NOTIFY, CLEAR_DROP, CLEAR_EXP;
    }

    /**
     * その場所から一定の範囲(config指定)で死んだモブの数が一定以上(config指定)に達したとき、その場所を処理槽とみなす。
     * ただし、SpawnReasonごとに別々の扱いとする。
     * 処理槽と判定されたとき、その場所付近で死んだすべてのモブのスポーン地点の範囲を湧き層とみなす。
     * 処理槽から一定以上離れた場所(config指定)でスポーンしたモブは弾かれ、その場所で死んだという扱いにしない。
     * 湧き層と処理槽の範囲がどちらも定まったとき、その場所をTTとみなし、SpawnReasonごとに指定されるFarmActionのリストを返す。
     * このイベントではまだ湧き層と処理槽が確定しない場合は空のリストを返す。
     * 
     * @param event イベント
     * @return このエンティティがTTで死んだ場合のアクションのリスト
     */
    private List<FarmAction> checkFarm(EntityDeathEvent event) {
        List<FarmAction> result = new ArrayList<>();
        // TODO: WIP
        return result;
    }
    
    @EventHandler
    private void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        entity.setMetadata("spawn_location", new FixedMetadataValue(plugin, locationToString(entity.getLocation())));
    }

    public Optional<Location> getSpawnLocation(LivingEntity entity) {
        for (MetadataValue meta : entity.getMetadata("spawn_location")) {
            if (meta.getOwningPlugin().equals(plugin)) {
                Optional.ofNullable(stringToLocation(meta.asString())); 
            }
        }
        return Optional.empty();
    }

    private String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private Location stringToLocation(String locationString) {
        String[] parts = locationString.split(",", -1);
        World world = plugin.getServer().getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        int x, y, z;
        try {
            x = Integer.parseInt(parts[1]);
            y = Integer.parseInt(parts[2]);
            z = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return null;
        }
        return new Location(world, x, y, z);
    }
}
