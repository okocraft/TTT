package net.okocraft.ttt.config;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.value.ConfigValue;

public final class Settings {

    private Settings() {
        throw new UnsupportedOperationException();
    }

    public static final ConfigValue<List<String>> SPAWNER_UNBREAKING_MOB_TYPES =
            config -> config.getStringList("spawner.unbreakable.mob-types");

    public static final ConfigValue<Boolean> SPAWNER_MINABLE_ENABLED =
            config -> config.getBoolean("spawner.minable.enabled");

    public static final ConfigValue<Boolean> SPAWNER_MINABLE_NEEDS_SILKTOUCH =
            config -> config.getBoolean("spawner.minable.needs-silktouch");

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

    public static final ConfigValue<Boolean> SPAWNER_CANCEL_CHANGING_MOB =
            config -> config.getBoolean("spawner.cancel-changing-mob");

    public static final ConfigValue<Map<String, Integer>> SPAWNER_TOTAL_SPAWNABLE_MOBS =
            config -> {
                Map<String, Integer> result = new HashMap<>();
                result.put("DEFAULT", 5000);
                Configuration totalSpawnableMobs = config.getSection("spawner.total-spawnable-mobs");
                if (totalSpawnableMobs == null) {
                    return result;
                }
                for (String key : totalSpawnableMobs.getKeyList()) {
                    try {
                        result.put(key.toUpperCase(Locale.ROOT), totalSpawnableMobs.getInteger(key.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                return result;
            };

    public static final ConfigValue<Boolean> MOB_STACKER_ENABLED =
            config -> config.getBoolean("mob-stacker.enabled");

    public static final ConfigValue<Boolean> MOB_STACKER_TARGET_SPAWN_REASON =
            config -> config.getBoolean("mob-stacker.target-spawn-reason");

    public static final ConfigValue<Boolean> FIND_TRAPS_ENABLED =
            config -> config.getBoolean("find-traps.enabled");

    public static final ConfigValue<List<String>> FIND_TRAPS_CHECK_SPAWN_REASON =
            config -> config.getStringList("find-traps.check-spawn-reason");

    public static final ConfigValue<Boolean> ANTI_CLICKBOT_ENABLED =
            config -> config.getBoolean("anti-clickbot.enabled");

    public static final ConfigValue<List<String>> ANTI_CLICKBOT_TYPE =
            config -> config.getStringList("anti-clickbot.type");

    public static final ConfigValue<Boolean> PREVENT_CRAMMING_DEATH_DROP =
            config -> config.getBoolean("prevent-cramming-death-drop");

}