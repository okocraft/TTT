package net.okocraft.ttt.config.worldsetting.farm;

import org.jetbrains.annotations.NotNull;

public record FarmSetting(boolean preventCrammingDeathDrop, @NotNull FinderSetting finderSetting) {

    public static final FarmSettingDeserializer DESERIALIZER = new FarmSettingDeserializer();

}
