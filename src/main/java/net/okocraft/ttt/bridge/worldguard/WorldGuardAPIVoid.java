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
    
}
