package net.okocraft.ttt.config.worldsetting.anticlickbot;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.serializer.ConfigurationSerializer;

import org.jetbrains.annotations.NotNull;

public class AntiClickBotSettingDeserializer implements ConfigurationSerializer<AntiClickBotSetting> {

    AntiClickBotSettingDeserializer() {
    }

    @Override
    public @NotNull Configuration serialize(@NotNull AntiClickBotSetting value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull AntiClickBotSetting deserializeConfiguration(@NotNull Configuration config) {
        int distanceThreshold = config.getInteger("anti-clickbot.distance-threshold");
        int killCountThreshold = config.getInteger("anti-clickbot.kill-count-threshold");
        int varificationTimeout = config.getInteger("anti-clickbot.varification-timeout");
        Punishment punishment;
        try {
            punishment = Punishment.valueOf(config.getString("anti-clickbot.punishment"));
        } catch (IllegalArgumentException e) {
            punishment = Punishment.SUMMON_ENEMY;
        }

        return new AntiClickBotSetting(distanceThreshold, killCountThreshold, varificationTimeout, punishment);
    }

}
