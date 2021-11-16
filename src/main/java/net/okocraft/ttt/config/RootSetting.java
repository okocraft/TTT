package net.okocraft.ttt.config;

import com.github.siroshun09.configapi.api.Configuration;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.okocraft.ttt.config.worldsetting.WorldSetting;

import java.util.HashMap;
import java.util.Map;

public class RootSetting {

    public static final RootSettingSerializer DESERIALIZER = new RootSettingSerializer();

    public static RootSetting DEFAULT_SETTING = new RootSetting(false, "");

    /**
     * The value of {@code Configuration.getSection("world-setting");}
     * 
     * @apiNote null for default config or config world-setting removed. Otherwise not null.
     */
    @Nullable
    private final Configuration configWorldSettings;

    private final boolean debug;

    private final String discordWebhookUrl;

    private final Map<World, WorldSetting> worldSettings = new HashMap<>();

    private RootSetting(boolean debug, String discordWebhookUrl) {
        this(null, debug, discordWebhookUrl);
    }

    public RootSetting(Configuration configWorldSettings, boolean debug, String discordWebhookUrl) {
        this.configWorldSettings = configWorldSettings;
        this.debug = debug;
        this.discordWebhookUrl = discordWebhookUrl;
    }

    public boolean debug() {
        return debug;
    }

    public String discordWebhookUrl() {
        return discordWebhookUrl;
    }
    
    public @NotNull WorldSetting worldSetting(@NotNull World world) {
        return worldSettings.computeIfAbsent(world, this::readWorldSetting);
    }

    private @NotNull WorldSetting readWorldSetting(@NotNull World world) {
        if (configWorldSettings == null) {
            return WorldSetting.DEFAULT_SETTING;
        }

        var section = configWorldSettings.getSection(world.getName());

        if (section != null) {
            return WorldSetting.DESERIALIZER.deserializeConfiguration(section);
        } else {
            return WorldSetting.DEFAULT_SETTING;
        }
    }
    
    public void clearWorldSettingCache() {
        worldSettings.clear();
    }
}
