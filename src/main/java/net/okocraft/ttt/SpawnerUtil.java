package net.okocraft.ttt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.github.siroshun09.configapi.api.Configuration;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.translation.GlobalTranslator;
import net.okocraft.ttt.config.Messages;
import net.okocraft.ttt.config.Settings;

public final class SpawnerUtil {

    private final TTT plugin;
    private final Configuration config;
    private final NamespacedKey entityKey;
    private final NamespacedKey spawnableMobsKey;
    
    SpawnerUtil(TTT plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.entityKey = NamespacedKey.fromString("entity", plugin);
        this.spawnableMobsKey = NamespacedKey.fromString("spawnablemobs", plugin);
    }

    public NamespacedKey getSpawnableMobsKey() {
        return spawnableMobsKey;
    }

    public void copyDataFromItem(CreatureSpawner spawner, ItemStack spawnerItem, boolean updateState) {
        if (spawnerItem.getType() != Material.SPAWNER) {
            return;
        }

        if (hasSpawnableMobsData(spawnerItem)) {
            setSpawnableMobs(spawner, getSpawnableMobs(spawnerItem));
        } else {
            setSpawnableMobs(spawner, getDefaultSpawnableMobs(spawner.getSpawnedType()));
        }
                
        Optional<EntityType> typeOpt = getEntityTypeFrom(spawnerItem);
        if (typeOpt.isPresent()) {
            spawner.setSpawnedType(typeOpt.get());
        } else {
            plugin.getLogger().warning("Illegal entity type spawner is placed at: " + spawner.getLocation());
            return;
        }

        if (updateState) {
            spawner.update();
        }
    }

    public ItemStack getSpawnerItemFrom(CreatureSpawner spawner, Locale locale) {
        return createSpawner(spawner.getSpawnedType(), getSpawnableMobs(spawner), locale);
    }

    public ItemStack createSpawner(EntityType type) {
        return createSpawner(type, Locale.US);
    }

    public ItemStack createSpawner(EntityType type, Locale locale) {
        return createSpawner(type, getDefaultSpawnableMobs(type), locale);
    }
    
    public int getDefaultSpawnableMobs(EntityType type) {
        Map<String, Integer> maxSpawnableMobs = plugin.getConfiguration().get(Settings.SPAWNER_TOTAL_SPAWNABLE_MOBS);
        return maxSpawnableMobs.getOrDefault(type.name(), maxSpawnableMobs.getOrDefault("DEFAULT", 5000));
    }

    public ItemStack createSpawner(EntityType type, int spawnableMobs, Locale locale) {
        ItemStack spawner = new ItemStack(Material.SPAWNER);
        setEntityType(spawner, type);
        setSpawnableMobs(spawner, spawnableMobs);
        changeLocale(spawner, locale);

        return spawner;
    }

    public void setEntityType(ItemStack spawnerItem, EntityType type) {
        if (spawnerItem.getType() == Material.SPAWNER) {
            ItemMeta meta = spawnerItem.getItemMeta();
            meta.getPersistentDataContainer().set(entityKey, PersistentDataType.STRING, type.name());
            spawnerItem.setItemMeta(meta);
        }
    }

    public void changeLocale(ItemStack spawner, Locale locale) {
        if (spawner.getType() != Material.SPAWNER) {
            return;
        }

        getEntityTypeFrom(spawner).ifPresent(type -> {
            ItemMeta meta = spawner.getItemMeta();
            meta.displayName(GlobalTranslator.render(Messages.SPAWNER_DISPLAY_NAME.apply(type), locale));
            meta.lore(List.of(GlobalTranslator.render(Messages.SPAWNER_LORE.apply(
                    getSpawnableMobs(spawner),
                    getDefaultSpawnableMobs(type)
            ), locale)));
            
            spawner.setItemMeta(meta);
        });
    }

    public Optional<EntityType> getEntityTypeFrom(ItemStack spawner) {
        ItemMeta meta = spawner.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }

        String type = meta.getPersistentDataContainer().get(entityKey, PersistentDataType.STRING);
        if (type == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(EntityType.valueOf(type));
        } catch(IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean hasSpawnableMobsData(CreatureSpawner spawner) {
        return spawner.getPersistentDataContainer().has(spawnableMobsKey, PersistentDataType.INTEGER);
    }

    public boolean hasSpawnableMobsData(ItemStack spawner) {
        if (spawner.getType() == Material.SPAWNER) {
            return spawner.getItemMeta().getPersistentDataContainer().has(spawnableMobsKey, PersistentDataType.INTEGER);
        } else {
            return false;
        }
    }

    public int getSpawnableMobs(ItemStack spawner) {
        if (spawner.getType() == Material.SPAWNER) {
            return spawner.getItemMeta().getPersistentDataContainer().getOrDefault(spawnableMobsKey, PersistentDataType.INTEGER, 5000);
        } else {
            return 0;
        }
    }

    public void setSpawnableMobs(ItemStack spawner, int limit) {
        if (spawner.getType() != Material.SPAWNER) {
            return;
        }
        ItemMeta meta = spawner.getItemMeta();
        meta.getPersistentDataContainer().set(spawnableMobsKey, PersistentDataType.INTEGER, limit);
        spawner.setItemMeta(meta);
    }

    public void decrementSpawnableMobs(ItemStack spawner) {
        setSpawnableMobs(spawner, getSpawnableMobs(spawner) - 1);
    }

    public void incrementSpawnableMobs(ItemStack spawner) {
        setSpawnableMobs(spawner, getSpawnableMobs(spawner) + 1);
    }

    public int getSpawnableMobs(CreatureSpawner spawner) {
        return spawner.getPersistentDataContainer().getOrDefault(spawnableMobsKey, PersistentDataType.INTEGER, 5000);
    }

    public void setSpawnableMobs(CreatureSpawner spawner, int limit) {
        spawner.getPersistentDataContainer().set(spawnableMobsKey, PersistentDataType.INTEGER, limit);
    }

    public void decrementSpawnableMobs(CreatureSpawner spawner) {
        setSpawnableMobs(spawner, getSpawnableMobs(spawner) - 1);
    }

    public void incrementSpawnableMobs(CreatureSpawner spawner) {
        setSpawnableMobs(spawner, getSpawnableMobs(spawner) + 1);
    }

    public boolean isRunning(CreatureSpawner spawner) {
        return !config.get(Settings.SPAWNER_STOPPED_BY_REDSTONE_SIGNAL_ENABLED_WORLDS)
                .contains(spawner.getWorld().getName())
                || isBlockPowered(spawner.getBlock()) != config
                        .get(Settings.SPAWNER_STOPPED_BY_REDSTONE_SIGNAL_REVERSE);
    }

    private boolean isBlockPowered(Block block) {
        for (BlockFace face : BlockFace.values()) {
            if (face.isCartesian()) {
                if (block.isBlockFacePowered(face) || block.isBlockFaceIndirectlyPowered(face)) {
                    return true;
                }
            }
        }

        return block.isBlockPowered() || block.isBlockIndirectlyPowered();
    }

    public List<CreatureSpawner> getSpawnersIn(int radius, Location center) {
        List<CreatureSpawner> result = new ArrayList<>();
        int chunks = (radius / 16) + 1;
        Chunk centerChunk = center.getChunk();
        for (int chunkX = centerChunk.getX() - chunks; chunkX <= centerChunk.getX() + chunks; chunkX++) {
            for (int chunkZ = centerChunk.getZ() - chunks; chunkZ <= centerChunk.getZ() + chunks; chunkZ++) {
                Chunk chunk = center.getWorld().getChunkAt(chunkX, chunkZ);
                for (BlockState tile : chunk.getTileEntities()) {
                    if (tile instanceof CreatureSpawner spawner) {
                        if (tile.getLocation().distanceSquared(center) <= radius * radius) {
                            result.add(spawner);
                        }
                    }
                }
            }
        }
        return result;
    }

    public LimitActions checkMineSpawner(Player player, CreatureSpawner spawner) {
        if (hasSpawnableMobsData(spawner)) {
            return LimitActions.ALLOW;
        }

        String worldName = spawner.getWorld().getName();
        String mobTypeName = spawner.getSpawnedType().name();
        int minedAmount = plugin.getPlayerData()
                .getInteger(player.getUniqueId().toString() + "." + worldName + "." + mobTypeName);

        Configuration section = config.getSection("spawner.mine-limiter");
        if (section == null) {
            return LimitActions.ALLOW;
        }

        for (String limiterName : config.getKeyList()) {
            if (!section.getString(limiterName + ".world").equalsIgnoreCase(worldName)) {
                continue;
            }

            if (!section.getString(limiterName + ".mob-type").equalsIgnoreCase(mobTypeName)) {
                continue;
            }

            if (section.getInteger(limiterName + ".amount") >= minedAmount) {
                try {
                    return LimitActions.valueOf(section.getString(limiterName + ".action", "CANCEL"));
                } catch (IllegalArgumentException e) {
                    return LimitActions.CANCEL;
                }
            }
        }

        return LimitActions.ALLOW;
    }

    public enum LimitActions {
        CANCEL, NO_DROP, ALLOW;
    }
}
