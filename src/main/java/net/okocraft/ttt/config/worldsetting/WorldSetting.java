package net.okocraft.ttt.config.worldsetting;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;

import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.config.worldsetting.anticlickbot.AntiClickBotSetting;
import net.okocraft.ttt.config.worldsetting.anticlickbot.Punishment;
import net.okocraft.ttt.config.worldsetting.farm.FarmSetting;
import net.okocraft.ttt.config.worldsetting.farm.FinderSetting;
import net.okocraft.ttt.config.worldsetting.spawner.IsolatingSetting;
import net.okocraft.ttt.config.worldsetting.spawner.RedstoneSwitchesSetting;
import net.okocraft.ttt.config.worldsetting.spawner.SpawnerSetting;

import java.util.Collections;

public record WorldSetting(@NotNull SpawnerSetting spawnerSetting, @NotNull FarmSetting farmSetting, @NotNull AntiClickBotSetting antiClickBotSetting) {

    public static final WorldSettingSerializer DESERIALIZER = new WorldSettingSerializer();

    public static WorldSetting DEFAULT_SETTING = new WorldSetting(
            new SpawnerSetting(
                    2,
                    Collections.emptyMap(),
                    100000,
                    Collections.emptyMap(),
                    new IsolatingSetting(true, 34, 2, 34, 2),
                    new RedstoneSwitchesSetting(false, false),
                    ImmutableTable.of()
            ),
            new FarmSetting(
                    true, new FinderSetting(2, 30, ImmutableMultimap.of())
            ),
            new AntiClickBotSetting(3, 150, 30, Punishment.SUMMON_ENEMY)
    );
}
