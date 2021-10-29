package net.okocraft.ttt.config.worldsetting.farm;

import com.google.common.collect.Multimap;

import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public record FinderSetting(int killingChumberRange, int killedMobsToBeKillingChumber,
                            @NotNull @Unmodifiable Multimap<CreatureSpawnEvent.SpawnReason, FarmAction> farmActions) {
}
