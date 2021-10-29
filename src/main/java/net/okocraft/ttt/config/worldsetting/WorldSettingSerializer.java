package net.okocraft.ttt.config.worldsetting;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.serializer.ConfigurationSerializer;

import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.config.worldsetting.farm.FarmSetting;
import net.okocraft.ttt.config.worldsetting.spawner.SpawnerSetting;

public class WorldSettingSerializer implements ConfigurationSerializer<WorldSetting> {

    @Override
    public @NotNull Configuration serialize(@NotNull WorldSetting value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull WorldSetting deserializeConfiguration(@NotNull Configuration config) {
        var spawnerSetting =
                config.get("spawner", SpawnerSetting.DESERIALIZER, WorldSetting.DEFAULT_SETTING.spawnerSetting());
        var farmSetting =
                config.get("farm", FarmSetting.DESERIALIZER, WorldSetting.DEFAULT_SETTING.farmSetting());

        return new WorldSetting(spawnerSetting, farmSetting);
    }
}
