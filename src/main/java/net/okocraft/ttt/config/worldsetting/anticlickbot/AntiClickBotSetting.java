package net.okocraft.ttt.config.worldsetting.anticlickbot;

public record AntiClickBotSetting(int distanceThreshold, int killCountThreshold, int verificationTimeout, Punishment punishment) {

    public static final AntiClickBotSettingDeserializer DESERIALIZER = new AntiClickBotSettingDeserializer();

}
