package net.okocraft.ttt.module.spawner;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.TTT;

public abstract class Spawner<T> {

    protected static TTT plugin;
    public static void setPlugin(TTT plugin) {
        Spawner.plugin = plugin;
    }

    protected static NamespacedKey entityKey;
    protected static NamespacedKey spawnableMobsKey;
    protected static NamespacedKey maxSpawnableMobsKey;

    protected final T spawner;

    protected Spawner(@NotNull T spawner) {
        this.spawner = spawner;
        if (entityKey == null || spawnableMobsKey == null || maxSpawnableMobsKey == null) {
            if (plugin == null) {
                throw new IllegalArgumentException("TTT plugin is not initialized yet.");
            }
            entityKey = NamespacedKey.fromString("entity", plugin);
            spawnableMobsKey = NamespacedKey.fromString("spawnablemobs", plugin);
            spawnableMobsKey = NamespacedKey.fromString("maxspawnablemobs", plugin);
        }
    }

    public T get() {
        return spawner;
    }

    public abstract void setSpawnedType(EntityType type);

    public abstract EntityType getSpawnedType();

    public abstract int getMaxSpawnableMobs();
    
    public abstract void setMaxSpawnableMobs(int maxSpawnableMobs);

    public abstract int getSpawnableMobs();

    public abstract void setSpawnableMobs(int spawnableMobs);

    public void decrementSpawnableMobs() {
        setSpawnableMobs(getSpawnableMobs() - 1);
    }

    public void incrementSpawnableMobs() {
        setSpawnableMobs(getSpawnableMobs() + 1);
    }
}
