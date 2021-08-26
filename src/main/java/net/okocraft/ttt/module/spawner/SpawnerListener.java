package net.okocraft.ttt.module.spawner;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import net.okocraft.ttt.SpawnerUtil;
import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.Messages;
import net.okocraft.ttt.config.Settings;

public class SpawnerListener implements Listener {

    private final TTT plugin;
    private final SpawnerUtil spawnerUtil;
    private final YamlConfiguration config;

    public SpawnerListener(TTT plugin) {
        this.plugin = plugin;
        this.spawnerUtil = plugin.getSpawnerUtil();
        this.config = plugin.getConfiguration();
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) {
            return;
        }
        CreatureSpawner state = (CreatureSpawner) block.getState();

        // unbreakable
        if (!event.getPlayer().hasPermission("ttt.bypass.unbreakable")) {
            Set<EntityType> unbreakableMobTypes;
            List<String> unbreakableMobTypeNames = config.get(Settings.SPAWNER_UNBREAKING_MOB_TYPES);
            if (unbreakableMobTypeNames.contains("ALL")) {
                unbreakableMobTypes = EnumSet.allOf(EntityType.class);
            } else {
                unbreakableMobTypes = new HashSet<>();
                for (String name : new ArrayList<String>()) {
                    try {
                        if (name != null && !name.isBlank()) {
                            unbreakableMobTypes.add(EntityType.valueOf(name.toUpperCase(Locale.ROOT)));
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger()
                        .warning("Config string list \"spawner.unbreakable.mob-type\" have illegal value: " + name);
                    }
                }
            }
            if (unbreakableMobTypes.contains(state.getSpawnedType())) {
                event.setCancelled(true);
                return;
            }
        }

        // flag
        // TODO: implement

        // mineable
        if (config.get(Settings.SPAWNER_MINABLE_ENABLED) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (!config.get(Settings.SPAWNER_MINABLE_NEEDS_SILKTOUCH) || event.getPlayer().getInventory()
                    .getItemInMainHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                ItemStack spawnerItem = spawnerUtil.getSpawnerItemFrom(state, event.getPlayer().locale());
                event.setExpToDrop(0);
                block.getWorld().dropItemNaturally(block.getLocation(), spawnerItem);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        ItemStack spawnerItem = event.getItemInHand();
        if (placed.getType() != Material.SPAWNER || spawnerItem.getType() != Material.SPAWNER) {
            return;
        }

        CreatureSpawner spawner = (CreatureSpawner) placed.getState();
        cancelEventIfNotIsolated(spawner, event);
        if (event.isCancelled()) {
            return;
        }
        
        spawnerUtil.copyDataFromItem(spawner, spawnerItem, true);
    }

    private void cancelEventIfNotIsolated(CreatureSpawner placed, BlockPlaceEvent event) {
        if (!config.get(Settings.SPAWNER_ISOLATING_ENABLED)) {
            return;
        }

        int maxSpawners = config.get(Settings.SPAWNER_ISOLATING_AMOUNT);
        int radius = config.get(Settings.SPAWNER_ISOLATING_RADIUS);

        if (spawnerUtil.getSpawnersIn(radius, placed.getLocation()).size() > maxSpawners) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Messages.TOO_MANY_SPAWNERS.apply(
                    config.get(Settings.SPAWNER_ISOLATING_RADIUS),
                    config.get(Settings.SPAWNER_ISOLATING_AMOUNT)
            ));
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SPAWNER) {
            return;
        }

        checkSpawnEggAndCancel(event);
        if (event.isCancelled()) {
            return;
        }

        if (event.getHand() == EquipmentSlot.OFF_HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        CreatureSpawner spawner = (CreatureSpawner) block.getState();

        event.getPlayer().sendMessage(Messages.SHOWN_SPAWNER_STATUS.apply(
                spawnerUtil.isRunning(spawner),
                spawner.getSpawnedType(),
                spawnerUtil.getSpawnableMobs(spawner),
                spawnerUtil.getDefaultSpawnableMobs(spawner.getSpawnedType())
        ));

        if (config.get(Settings.SPAWNER_STOPPED_BY_REDSTONE_SIGNAL_ENABLED_WORLDS).contains(block.getWorld().getName())) {
            if (config.get(Settings.SPAWNER_STOPPED_BY_REDSTONE_SIGNAL_REVERSE)) {
                event.getPlayer().sendMessage(Messages.SPAWNER_START_TIP_REVERSE);
            } else {
                event.getPlayer().sendMessage(Messages.SPAWNER_START_TIP);
            }
        }
    }

    private void checkSpawnEggAndCancel(PlayerInteractEvent event) {
        ItemMeta meta = event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
        if (!(meta instanceof SpawnEggMeta)) {
            return;
        }

        if (config.get(Settings.SPAWNER_CANCEL_CHANGING_MOB) && !event.getPlayer().hasPermission("ttt.bypass.changespawner")) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.SPAWNER) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.CANNOT_CHANGE_SPAWNER);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerSpawnedMob(SpawnerSpawnEvent event) {
        CreatureSpawner spawner = event.getSpawner();
        if (!spawnerUtil.isRunning(spawner)) {
            event.setCancelled(true);
            return;
        }
        
        if (spawnerUtil.getSpawnableMobs(spawner) <= 0) {
            event.setCancelled(true);
        } else {
            spawnerUtil.decrementSpawnableMobs(spawner);
        }

        spawner.update();
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryOpen(InventoryOpenEvent event) {
        for (ItemStack item : event.getInventory()) {
            if (item != null && item.getType() == Material.SPAWNER) {
                spawnerUtil.changeLocale(item, ((Player) event.getPlayer()).locale());
            }
        }
    }
}
