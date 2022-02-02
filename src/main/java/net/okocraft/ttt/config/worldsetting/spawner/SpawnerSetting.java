package net.okocraft.ttt.config.worldsetting.spawner;

import com.google.common.collect.Table;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

public record SpawnerSetting(
        boolean minableSpawnerLimitEnabled,
        int defaultMaxMinableSpawners,
        @NotNull Map<EntityType, Integer> maxMinableSpawners,
        int defaultMaxSpawnableMobs,
        @NotNull Map<EntityType, Integer> maxSpawnableMobs,
        @NotNull IsolatingSetting isolatingSetting,
        @NotNull RedstoneSwitchesSetting redstoneSwitchesSetting,
        /** Row is source type, Column is mapped type and the value is weight. */
        @NotNull @Unmodifiable Table<EntityType, EntityType, Double> typeMapping
) {

    public static final SpawnerSettingDeserializer DESERIALIZER = new SpawnerSettingDeserializer();

    public int maxMinableSpawners(EntityType type) {
        return maxMinableSpawners.getOrDefault(type, defaultMaxMinableSpawners);
    }

    public int maxSpawnableMobs(EntityType type) {
        return maxSpawnableMobs.getOrDefault(type, defaultMaxSpawnableMobs);
    }

}
