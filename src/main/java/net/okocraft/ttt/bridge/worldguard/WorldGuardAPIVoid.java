package net.okocraft.ttt.bridge.worldguard;

import org.bukkit.entity.Player;

public class WorldGuardAPIVoid implements WorldGuardAPI {

    @Override
    public boolean registerCustomFlags() {
        return false;
    }

    @Override
    public boolean canAttack(Player player) {
        return true;
    }
    // okocraft ancient - add spawner mob limiter
    @Override
    public @org.jetbrains.annotations.Nullable org.bukkit.util.BoundingBox getRegion(@org.jetbrains.annotations.NotNull org.bukkit.Location location) {
        return null;
    }
}
