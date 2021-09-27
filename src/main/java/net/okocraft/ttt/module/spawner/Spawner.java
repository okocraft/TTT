package net.okocraft.ttt.module.spawner;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.TTT;

public abstract class Spawner<T> {

    protected static TTT plugin;

    protected static NamespacedKey entityKey;
    protected static NamespacedKey spawnableMobsKey;
    protected static NamespacedKey maxSpawnableMobsKey;

    public static void initialize(TTT plugin) {
        Spawner.plugin = plugin;
        
        Spawner.entityKey = NamespacedKey.fromString("entity", plugin);
        Spawner.spawnableMobsKey = NamespacedKey.fromString("spawnablemobs", plugin);
        Spawner.maxSpawnableMobsKey = NamespacedKey.fromString("maxspawnablemobs", plugin);
    }

    protected final T spawner;

    protected Spawner(@NotNull T spawner) {
        this.spawner = spawner;
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
