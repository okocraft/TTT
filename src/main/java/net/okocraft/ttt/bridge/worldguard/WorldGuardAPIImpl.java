package net.okocraft.ttt.bridge.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardAPIImpl implements WorldGuardAPI {

    private final StringFlag disabledCustomEnchantsFlag = new StringFlag("disabled-custom-enchants");

    @Override
    public boolean registerCustomFlags() {
        try {
            WorldGuard.getInstance().getFlagRegistry().register(disabledCustomEnchantsFlag);
            return true;
        } catch (FlagConflictException ignored) {
            return false;
        }
    }

    private ApplicableRegionSet createRegionSet(Location location) {
        if (location.getWorld() == null) {
            return null;
        }
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(location.getWorld()));
        return new RegionResultSet(
            regionManager.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint()).getRegions(),
            regionManager.getRegion("__global__")
        );
    }

    @Override
    public boolean canAttack(Player player) {
        return createRegionSet(player.getLocation()).queryState(
            WorldGuardPlugin.inst().wrapPlayer(player),
            Flags.PVP
        ) != State.DENY;
    }
}
