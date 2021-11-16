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
        int distanceThreshold = config.getInteger("distance-threshold");
        int killCountThreshold = config.getInteger("kill-count-threshold");
        int verificationTimeout = config.getInteger("verification-timeout");
        Punishment punishment;
        try {
            punishment = Punishment.valueOf(config.getString("punishment"));
        } catch (IllegalArgumentException e) {
            punishment = Punishment.SUMMON_ENEMY;
        }

        return new AntiClickBotSetting(distanceThreshold, killCountThreshold, verificationTimeout, punishment);
    }

}
