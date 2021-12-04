package net.okocraft.ttt.module.spawner;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.worldsetting.spawner.RedstoneSwitchesSetting;

public class SpawnerState extends Spawner<CreatureSpawner> {

    private SpawnerState(@NotNull CreatureSpawner spawner) {
        super(spawner);
    }

    public static boolean isValid(CreatureSpawner spawner) {
        if (spawner == null) {
            return false;
        }

        PersistentDataContainer dataContainer = spawner.getPersistentDataContainer();
        if (!dataContainer.has(maxSpawnableMobsKey, PersistentDataType.INTEGER)) {
            return false;
        }
        if (!dataContainer.has(spawnableMobsKey, PersistentDataType.INTEGER)) {
            return false;
        }
        return true;
    }

    public static SpawnerState from(@NotNull CreatureSpawner creatureSpawner) {
        SpawnerState spawner = new SpawnerState(creatureSpawner);
        if (!isValid(creatureSpawner)) {
            spawner.setMaxSpawnableMobs(TTT.getPlugin(TTT.class).getSetting()
                    .worldSetting(creatureSpawner.getWorld())
                    .spawnerSetting()
                    .maxSpawnableMobs(spawner.getSpawnedType()));
            spawner.setSpawnableMobs(spawner.getSpawnableMobs());
        }
        return spawner;
    }

    public void copyFrom(SpawnerItem spawnerItem) {
        setMaxSpawnableMobs(spawnerItem.getMaxSpawnableMobs());
        setSpawnableMobs(spawnerItem.getSpawnableMobs());
        setSpawnedType(spawnerItem.getSpawnedType());
    }

    @Override
    public void setSpawnedType(EntityType type) {
        spawner.setSpawnedType(type);
    }

    @Override
    public EntityType getSpawnedType() {
        return spawner.getSpawnedType();
    }

    @Override
    public int getMaxSpawnableMobs() {
        return spawner.getPersistentDataContainer().getOrDefault(maxSpawnableMobsKey, PersistentDataType.INTEGER, 100000);
    }
    
    @Override
    public void setMaxSpawnableMobs(int maxSpawnableMobs) {
        spawner.getPersistentDataContainer().set(maxSpawnableMobsKey, PersistentDataType.INTEGER, maxSpawnableMobs);

        if (getSpawnableMobs() > maxSpawnableMobs) {
            setSpawnableMobs(maxSpawnableMobs);
        }
    }

    @Override
    public int getSpawnableMobs() {
        return spawner.getPersistentDataContainer().getOrDefault(spawnableMobsKey, PersistentDataType.INTEGER, getMaxSpawnableMobs());
    }

    @Override
    public void setSpawnableMobs(int spawnableMobs) {
        int maxSpawnableMobs = getMaxSpawnableMobs();
        if (spawnableMobs > maxSpawnableMobs) {
            spawnableMobs = maxSpawnableMobs;
        }
        spawner.getPersistentDataContainer().set(spawnableMobsKey, PersistentDataType.INTEGER, spawnableMobs);
    }

    public boolean isRunning() {
        RedstoneSwitchesSetting redstoneSwitches = plugin.getSetting().worldSetting(spawner.getWorld())
                .spawnerSetting().redstoneSwitchesSetting();
        return !redstoneSwitches.enabled() || isBlockPowered(spawner.getBlock()) == redstoneSwitches.reversed();
    }

    private static boolean isBlockPowered(Block block) {
        for (BlockFace face : BlockFace.values()) {
            if (face.isCartesian()) {
                if (block.isBlockFacePowered(face) || block.isBlockFaceIndirectlyPowered(face)) {
                    return true;
                }
            }
        }

        return block.isBlockPowered() || block.isBlockIndirectlyPowered();
    }

    public static List<CreatureSpawner> getSpawnersIn(int radius, Location center) {
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
}
