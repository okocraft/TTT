package net.okocraft.ttt.worldguard;

import org.bukkit.entity.Player;

public interface WorldGuardAPI {

    boolean registerCustomFlags();
    boolean canAttack(Player player);

}
