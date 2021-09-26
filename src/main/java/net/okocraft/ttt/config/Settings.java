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

    public static final ConfigValue<Boolean> SPAWNER_STOPPED_BY_REDSTONE_SIGNAL_REVERSE =
            config -> config.getBoolean("spawner.stopped-by-redstone-signal.reverse");

    public static final ConfigValue<List<String>> SPAWNER_STOPPED_BY_REDSTONE_SIGNAL_ENABLED_WORLDS =
            config -> config.getStringList("spawner.stopped-by-redstone-signal.enabled-worlds");
    
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

    public static final ConfigValue<List<String>> SPAWNER_UNPLACEABLE_WORLDS =
            config -> config.getStringList("spawner-unplaceable-worlds");

    public static final ConfigValue<Boolean> MOB_STACKER_ENABLED =
            config -> config.getBoolean("mob-stacker.enabled");

    public static final ConfigValue<Boolean> MOB_STACKER_TARGET_SPAWN_REASON =
            config -> config.getBoolean("mob-stacker.target-spawn-reason");

    public static final ConfigValue<Boolean> FIND_FARMS_ENABLED =
            config -> config.getBoolean("find-farms.enabled");

    public static final ConfigValue<Integer> FIND_FARMS_KILLING_CHUMBER_RANGE =
            config -> config.getInteger("find-farms.killing-chumber-range");

    public static final ConfigValue<Integer> FIND_FARMS_KILLED_MOBS_TO_BE_KILLING_CHUMBER =
            config -> config.getInteger("find-farms.killed-mobs-to-be-killing-chumber");

    public static final ConfigValue<Map<SpawnReason, List<FindFarmsAction>>> FIND_FARMS_FARM_ACTIONS =
            config -> {
                Map<SpawnReason, List<FindFarmsAction>> result = new HashMap<>();
                for (SpawnReason reason : SpawnReason.values()) {
                    List<FindFarmsAction> actions = new ArrayList<>();
                    for (String farmActionName : config.getStringList("find-farms.farm-actions." + reason.name())) {
                        try {
                            actions.add(FindFarmsAction.valueOf(farmActionName.toUpperCase(Locale.ROOT)));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    result.put(reason, actions);
                }
                return result;
            };

    public static final ConfigValue<Boolean> ANTI_CLICKBOT_ENABLED =
            config -> config.getBoolean("anti-clickbot.enabled");

    public static final ConfigValue<List<String>> ANTI_CLICKBOT_TYPE =
            config -> config.getStringList("anti-clickbot.type");

    public static final ConfigValue<Boolean> PREVENT_CRAMMING_DEATH_DROP =
            config -> config.getBoolean("prevent-cramming-death-drop");

}