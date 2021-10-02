package net.okocraft.ttt.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.value.ConfigValue;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.okocraft.ttt.config.enums.FindFarmsAction;

public final class Settings {

    private Settings() {
        throw new UnsupportedOperationException();
    }

    public static final ConfigValue<Boolean> SPAWNER_ISOLATING_ENABLED =
            config -> config.getBoolean("spawner.isolating.enabled");

    public static final ConfigValue<Integer> SPAWNER_ISOLATING_RADIUS =
            config -> config.getInteger("spawner.isolating.radius");

    public static final ConfigValue<Integer> SPAWNER_ISOLATING_AMOUNT =
            config -> config.getInteger("spawner.isolating.amount");

    public static final ConfigValue<Integer> SPAWNER_ISOLATING_PLOTSQUARED_RADIUS =
            config -> config.getInteger("spawner.isolating.plotsquared.radius");

    public static final ConfigValue<Integer> SPAWNER_ISOLATING_PLOTSQUARED_AMOUNT =
            config -> config.getInteger("spawner.isolating.plotsquared.amount");

    public static boolean isRedstoneSpawnerSwitchEnabled(Configuration config, World world) {
        String worldName = world == null ? "default" : world.getName(); 
        return config.getBoolean("spawner.redstone-switches-spawner." + worldName + ".enabled", false);
    }
    
    public static boolean isRedstoneSpawnerSwitchReversed(Configuration config, World world) {
        String worldName = world == null ? "default" : world.getName(); 
        return config.getBoolean("spawner.redstone-switches-spawner." + worldName + ".reversed", false);
    }
    
    // TODO: move
    @NotNull
    @SuppressWarnings("unchecked")
    private static <T> T getPerWorldPerEntityTypeValue(@NotNull Configuration config, @NotNull String rootKey, @Nullable World world, @Nullable EntityType entityType, @NotNull T def) {
        Configuration worldSection = null;
        if (world != null) {
            worldSection = config.getSection(rootKey + "." + world.getName());
        }
        if (worldSection == null) {
            worldSection = config.getSection(rootKey + ".default");
            if (worldSection == null) {
                return def;
            }
        }

        try {
            if (entityType == null) {
                return (T) worldSection.get("DEFAULT", def);
            } else {
                return (T) worldSection.get(entityType.name(), worldSection.get("DEFAULT", def));
            }
        } catch (ClassCastException e) {
            return def;
        }
    }

    // TODO: move
    public static int getMaxSpawnableMobs(@NotNull Configuration config, @Nullable World world, @Nullable EntityType entityType) {
        return getPerWorldPerEntityTypeValue(config, "spawner.max-spawnable-mobs", world, entityType, 100000);
    }
    
    // TODO: move
    public static int getMaxMinableSpawners(@NotNull Configuration config, @Nullable World world, @Nullable EntityType entityType) {
        return getPerWorldPerEntityTypeValue(config, "spawner.max-minable-spawners", world, entityType, 2);
    }

    public static Map<EntityType, Double> getSpawnerTypeMapping(@NotNull Configuration config, @Nullable World world, @NotNull EntityType entityType) {
        Map<EntityType, Double> weightMap = new HashMap<>();
        String worldName = world == null ? "default" : world.getName();
        Configuration typeMappingSection = config.getSection("spawner.type-mapping." + worldName + "." + entityType.name());
        if (typeMappingSection == null) {
            typeMappingSection = config.getSection("spawner.type-mapping.default." + entityType.name());
            if (typeMappingSection == null) {
                return weightMap;
            } 
        }
        for (String key : typeMappingSection.getKeyList()) {
            try {
                weightMap.put(EntityType.valueOf(key), typeMappingSection.getDouble(key, 0));
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return weightMap;
    }

    public static final ConfigValue<Boolean> FARM_PREVENT_CRAMMING_DEATH_DROP =
            config -> config.getBoolean("farm.prevent-cramming-death-drop");

    public static final ConfigValue<Boolean> FARM_FINDER_ENABLED =
            config -> config.getBoolean("farms.finder.enabled");

    public static final ConfigValue<Integer> FARM_FINDER_KILLING_CHUMBER_RANGE =
            config -> config.getInteger("farm.finder.killing-chumber-range");

    public static final ConfigValue<Integer> FARM_FINDER_KILLED_MOBS_TO_BE_KILLING_CHUMBER =
            config -> config.getInteger("farm.finder.killed-mobs-to-be-killing-chumber");

    public static final ConfigValue<Map<SpawnReason, List<FindFarmsAction>>> FARM_FINDER_FARM_ACTIONS =
            config -> {
                Map<SpawnReason, List<FindFarmsAction>> result = new HashMap<>();
                for (SpawnReason reason : SpawnReason.values()) {
                    List<FindFarmsAction> actions = new ArrayList<>();
                    for (String farmActionName : config.getStringList("farm.finder.farm-actions." + reason.name())) {
                        try {
                            actions.add(FindFarmsAction.valueOf(farmActionName.toUpperCase(Locale.ROOT)));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    result.put(reason, actions);
                }
                return result;
            };

    public static final ConfigValue<Boolean> FARM_MOB_STACKER_ENABLED =
            config -> config.getBoolean("farm.mob-stacker.enabled");

    public static final ConfigValue<String> FARM_MOB_STACKER_TARGET_SPAWN_REASON =
            config -> config.getString("farm.mob-stacker.target-spawn-reason");

    public static final ConfigValue<Boolean> ANTI_CLICKBOT_ENABLED =
            config -> config.getBoolean("anti-clickbot.enabled");

    public static final ConfigValue<List<String>> ANTI_CLICKBOT_TYPE =
            config -> config.getStringList("anti-clickbot.type");
}