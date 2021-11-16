package net.okocraft.ttt.config.worldsetting.farm;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.serializer.ConfigurationSerializer;
import com.github.siroshun09.configapi.api.serializer.StringSerializer;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.okocraft.ttt.TTT;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Locale;
import java.util.logging.Logger;

public class FarmSettingDeserializer implements ConfigurationSerializer<FarmSetting> {

    private static final Logger LOGGER = JavaPlugin.getPlugin(TTT.class).getLogger();

    FarmSettingDeserializer() {
    }

    private static final StringSerializer<FarmAction> FARM_ACTION_SERIALIZER =
            new StringSerializer<>() {
                @Override
                public @NotNull String serialize(@NotNull FarmAction value) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public @Nullable FarmAction deserializeString(@NotNull String source) {
                    try {
                        return FarmAction.valueOf(source.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }
            };

    @Override
    public @NotNull Configuration serialize(@NotNull FarmSetting value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull FarmSetting deserializeConfiguration(@NotNull Configuration config) {
        boolean preventCrammingDeathDrop = config.getBoolean("prevent-cramming-death-drop");

        int killingChumberRange = config.getInteger("finder.killing-chumber-range");
        int killedMobsToBeKillingChumber = config.getInteger("finder.killed-mobs-to-be-killing-chumber");

        var farmActions = readFarmActions(config.getSection("finder.farm-actions"));

        return new FarmSetting(preventCrammingDeathDrop, new FinderSetting(killingChumberRange, killedMobsToBeKillingChumber, farmActions));
    }

    @NotNull
    @Unmodifiable
    private Multimap<SpawnReason, FarmAction> readFarmActions(@Nullable Configuration section) {
        if (section == null) {
            return ImmutableMultimap.of();
        }

        ImmutableMultimap.Builder<SpawnReason, FarmAction> builder = ImmutableMultimap.builder();

        for (var key : section.getKeyList()) {
            SpawnReason reason;

            try {
                reason = SpawnReason.valueOf(key.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Invalid spawn reason: " + key);
                continue;
            }

            builder.putAll(reason, section.getList(key, FARM_ACTION_SERIALIZER));
        }

        return builder.build();
    }
}
