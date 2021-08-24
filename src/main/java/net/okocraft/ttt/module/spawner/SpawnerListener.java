package net.okocraft.ttt.module.spawner;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataType;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.Settings;

public class SpawnerListener implements Listener {

    private final TTT plugin;
    private final YamlConfiguration config;

    public SpawnerListener(TTT plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) {
            return;
        }
        CreatureSpawner state = (CreatureSpawner) block.getState();

        // unbreakable
        Set<EntityType> unbreakableMobTypes;
        List<String> unbreakableMobTypeNames = config.get(Settings.SPAWNER_UNBREAKING_MOB_TYPES);
        if (unbreakableMobTypeNames.contains("ALL")) {
            unbreakableMobTypes = EnumSet.allOf(EntityType.class);
        } else {
            unbreakableMobTypes = new HashSet<>();
            for (String name : new ArrayList<String>()) {
                try {
                    if (name != null && !name.isBlank()) {
                        unbreakableMobTypes.add(EntityType.valueOf(name.toUpperCase(Locale.ROOT)));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger()
                            .warning("Config string list \"spawner.unbreakable.mob-type\" have illegal value: " + name);
                }
            }
        }
        if (unbreakableMobTypes.contains(state.getSpawnedType())) {
            event.setCancelled(true);
            return;
        }

        // flag
        // TODO: implement

        // mineable
        if (config.get(Settings.SPAWNER_MINABLE_ENABLED)) {
            if (!config.get(Settings.SPAWNER_MINABLE_NEEDS_SILKTOUCH) || event.getPlayer().getInventory().getItemInMainHand()
                    .getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                event.setExpToDrop(0);
                block.getWorld().dropItemNaturally(block.getLocation(),
                        plugin.getSpawnerUtil().createSpawner(state.getSpawnedType()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        if (placed.getType() != Material.SPAWNER) {
            return;
        }

        CreatureSpawner spawner = (CreatureSpawner) placed.getState();
        cancelEventIfNotIsolated(spawner, event);
        if (event.isCancelled()) {
            return;
        }

        Map<EntityType, Integer> maxTotalMobs = new HashMap<>();
        spawner.getPersistentDataContainer().set(NamespacedKey.fromString("maxtotal", plugin), PersistentDataType.INTEGER, maxTotalMobs.get(spawner.getSpawnedType()));
        spawner.update();

        Optional<EntityType> typeOpt = plugin.getSpawnerUtil().getEntityTypeFrom(event.getItemInHand());
        if (typeOpt.isPresent()) {
            spawner.setSpawnedType(typeOpt.get());
        } else {
            plugin.getLogger().warning("The player " + event.getPlayer() + " placed illegal entity type spawner");
            event.setCancelled(true);
            return;
        }

    }

    private void cancelEventIfNotIsolated(CreatureSpawner placed, BlockPlaceEvent event) {
        if (!config.get(Settings.SPAWNER_ISOLATING_ENABLED)) {
            return;
        }

        int maxSpawners = config.get(Settings.SPAWNER_ISOLATING_AMOUNT);
        int radius = config.get(Settings.SPAWNER_ISOLATING_RADIUS);
        int chunks = (radius / 16) + 1;
        Chunk placedChunk = placed.getChunk();

        int spawnersInRadius = 0;
        for (int chunkX = placedChunk.getX() - chunks; chunkX <= placedChunk.getX() + chunks; chunkX++) {
            for (int chunkZ = placedChunk.getZ() - chunks; chunkZ <= placedChunk.getZ() + chunks; chunkZ++) {
                Chunk chunk = placed.getWorld().getChunkAt(chunkX, chunkZ);

                for (BlockState tile : chunk.getTileEntities()) {
                    if (tile instanceof CreatureSpawner) {
                        if (tile.getLocation().distanceSquared(placed.getLocation()) < radius * radius) {
                            spawnersInRadius++;
                            if (maxSpawners < spawnersInRadius) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        ItemMeta meta = event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
        if (!(meta instanceof SpawnEggMeta)) {
            return;
        }

        if (config.get(Settings.SPAWNER_CANCEL_CHANGING_MOB) && !event.getPlayer().hasPermission("")) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.SPAWNER) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerSpawnedMob(SpawnerSpawnEvent event) {
        Block spawner = event.getSpawner().getBlock();
        for (BlockFace face : BlockFace.values()) {
            if (face.isCartesian()) {
                if (spawner.isBlockFacePowered(face) || spawner.isBlockFaceIndirectlyPowered(face)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (spawner.isBlockPowered() || spawner.isBlockIndirectlyPowered()) {
            event.setCancelled(true);
            return;
        }

        // TODO: decrease total max amount.
    }
}
