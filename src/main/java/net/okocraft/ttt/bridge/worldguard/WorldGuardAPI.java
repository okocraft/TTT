package net.okocraft.ttt.bridge.worldguard;

import org.bukkit.entity.Player;

public interface WorldGuardAPI {

    boolean registerCustomFlags();
    boolean canAttack(Player player);
    // okocraft ancient - add spawner mob limiter
    @org.jetbrains.annotations.Nullable org.bukkit.util.BoundingBox getRegion(@org.jetbrains.annotations.NotNull org.bukkit.Location location);

}
