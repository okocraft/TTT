package net.okocraft.ttt.config.worldsetting.spawner;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.serializer.ConfigurationSerializer;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import net.okocraft.ttt.TTT;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class SpawnerSettingDeserializer implements ConfigurationSerializer<SpawnerSetting> {

    private static final Logger LOGGER = JavaPlugin.getPlugin(TTT.class).getLogger();

    SpawnerSettingDeserializer() {
    }

    @Override
    public @NotNull Configuration serialize(@NotNull SpawnerSetting value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull SpawnerSetting deserializeConfiguration(@NotNull Configuration config) {
        var isolatingSetting = new IsolatingSetting(
                config.getBoolean("isolating.enabled"),
                config.getInteger("isolating.radius"),
                config.getInteger("isolating.amount"),
                config.getInteger("isolating.plotsquared.radius"),
                config.getInteger("isolating.plotsquared.amount")
        );

        var redstoneSwitchesSetting = new RedstoneSwitchesSetting(
                config.getBoolean("redstone-switches-spawner.enabled", false),
                config.getBoolean("redstone-switches-spawner.reversed", false)
        );

        return new SpawnerSetting(
                config.getBoolean("max-minable-spawners.enabled", true),
                config.getInteger("max-minable-spawners.DEFAULT"),
                readEntityValues(config.getSection("max-minable-spawners")),
                config.getInteger("max-spawnable-mobs.DEFAULT"),
                readEntityValues(config.getSection("max-spawnable-mobs")),
                isolatingSetting,
                redstoneSwitchesSetting,
                readTypeMapping(config.getSection("type-mapping"))
        );
    }

    private @NotNull Map<EntityType, Integer> readEntityValues(@Nullable Configuration section) {
        if (section == null) {
            return Collections.emptyMap();
        }

        var keys = section.getKeyList();
        var map = new HashMap<EntityType, Integer>(keys.size());

        for (var key : keys) {
            if (!key.equalsIgnoreCase("DEFAULT")) {
                toEntityType(key).ifPresent(type -> map.put(type, section.getInteger(key)));
            }
        }

        return map;
    }

    private @NotNull @Unmodifiable Table<EntityType, EntityType, Double> readTypeMapping(@Nullable Configuration section) {
        if (section == null) {
            return ImmutableTable.of();
        }

        ImmutableTable.Builder<EntityType, EntityType, Double> builder = ImmutableTable.builder();

        for (var key : section.getKeyList()) {
            if (key.equalsIgnoreCase("DEFAULT")) {
                continue;
            }

            var rootType = toEntityType(key);

            if (rootType.isPresent()) {
                var typeSection = section.getSection(key);

                if (typeSection != null) {
                    for (var typeName : typeSection.getKeyList()) {
                        toEntityType(typeName)
                                .ifPresent(type -> builder.put(rootType.get(), type, typeSection.getDouble(typeName)));
                    }
                }
            }
        }

        return builder.build();
    }

    private @NotNull Optional<EntityType> toEntityType(@NotNull String name) {
        try {
            return Optional.of(EntityType.valueOf(name.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid mob type: " + name);
            return Optional.empty();
        }
    }
}
