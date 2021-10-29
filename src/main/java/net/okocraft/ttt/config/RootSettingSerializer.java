package net.okocraft.ttt.config;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.serializer.ConfigurationSerializer;
import org.jetbrains.annotations.NotNull;

public class RootSettingSerializer implements ConfigurationSerializer<RootSetting> {

    RootSettingSerializer() {
    }

    @Override
    public @NotNull Configuration serialize(@NotNull RootSetting value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull RootSetting deserializeConfiguration(@NotNull Configuration config) {
        Configuration configWorldSettings = config.getSection("world-setting");
        boolean debug = config.getBoolean("debug");

        return new RootSetting(configWorldSettings, debug);
    }

}
