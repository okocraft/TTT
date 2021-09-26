package net.okocraft.ttt.module.spawner;

import java.util.Locale;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataContainer;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.Messages;
import net.okocraft.ttt.config.Settings;

public class SpawnerListener implements Listener {

    private final TTT plugin;
    private final YamlConfiguration config;

    public SpawnerListener(TTT plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (!(block.getState() instanceof CreatureSpawner state)) {
            return;
        }
        
        // TODO: temp code
        if (!SpawnerState.isValid(state)) {
            PersistentDataContainer container = state.getPersistentDataContainer();
            for (NamespacedKey key : container.getKeys()) {
                if (key.getNamespace().equals(plugin.getName().toLowerCase(Locale.ROOT))) {
                    container.remove(key);
                }
            }
        }

        if (!player.hasPermission("ttt.spawner.break." + state.getSpawnedType().getKey().getKey())) {
            event.setCancelled(true);
            return;
        }

        // flag
        // TODO: implement

        // mined spawner amount
        Configuration playerData = plugin.getPlayerData().getSection(player.getUniqueId() + ".mined-spawners." + block.getWorld().getUID());
        int minedSpawners;
        if (playerData != null) {
            minedSpawners = playerData.getInteger(state.getSpawnedType().name());
            if (minedSpawners >= Settings.getMaxMinableSpawners(config, block.getWorld(), state.getSpawnedType())) {
                event.setCancelled(true);
                return;
            }
        } else {
            minedSpawners = 0;
        }

        if (player.hasPermission("ttt.spawner.drop.withoutsilktouch")
                || player.getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
            event.setExpToDrop(0);
            block.getWorld().dropItemNaturally(
                    block.getLocation(),
                    SpawnerItem.from(state).getWithLocale(event.getPlayer().locale())
            );

            minedSpawners++;
            plugin.getPlayerData().set(player.getUniqueId() + ".mined-spawners." + block.getWorld().getUID() + "." + state.getSpawnedType().name(), minedSpawners);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        if (placed.getType() != Material.SPAWNER) {
            return;
        }

        CreatureSpawner spawner = (CreatureSpawner) placed.getState();
        cancelEventIfNotIsolated(spawner, event);
        if (event.isCancelled()) {
            return;
        }

        ItemStack item = event.getItemInHand();
        if (!SpawnerItem.isValid(item)) {
            if (!event.getPlayer().hasPermission("ttt.spawner.place.pig")) {
                event.setCancelled(true);
            }
            return;
        }

        SpawnerItem spawnerItem = SpawnerItem.from(item);
        if (!event.getPlayer().hasPermission("ttt.spawner.place." + spawnerItem.getSpawnedType().getKey().getKey())) {
            event.setCancelled(true);
            return;
        }

        SpawnerState.from(spawner).copyFrom(spawnerItem);
        spawner.update();     
    }

    private void cancelEventIfNotIsolated(CreatureSpawner placed, BlockPlaceEvent event) {
        if (!config.get(Settings.SPAWNER_ISOLATING_ENABLED)) {
            return;
        }

        int maxSpawners = config.get(Settings.SPAWNER_ISOLATING_AMOUNT);
        int radius = config.get(Settings.SPAWNER_ISOLATING_RADIUS);

        if (SpawnerState.getSpawnersIn(radius, placed.getLocation()).size() > maxSpawners) {
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
        if (event.useItemInHand() == Result.DENY) {
            return;
        }

        if (event.getHand() == EquipmentSlot.OFF_HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        if (!SpawnerState.isValid(spawner)) {
            return;
        }

        if (!event.getPlayer().hasPermission("ttt.spawner.showstatus")) {
            return;
        }

        event.getPlayer().sendMessage(Messages.SHOWN_SPAWNER_STATUS.apply(SpawnerState.from(spawner)));

        if (config.get(Settings.SPAWNER_STOPPED_BY_REDSTONE_SIGNAL_ENABLED_WORLDS).contains(block.getWorld().getName())) {
            if (config.get(Settings.SPAWNER_STOPPED_BY_REDSTONE_SIGNAL_REVERSE)) {
                event.getPlayer().sendMessage(Messages.SPAWNER_START_TIP_REVERSE);
            } else {
                event.getPlayer().sendMessage(Messages.SPAWNER_START_TIP);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void checkSpawnEggAndCancel(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        ItemMeta meta = event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
        if (clickedBlock != null && clickedBlock.getType() == Material.SPAWNER
                && meta instanceof SpawnEggMeta eggMeta && event.getPlayer().hasPermission("ttt.spawner.change." + eggMeta.getSpawnedType())) {
            event.setUseItemInHand(Result.DENY);
            event.getPlayer().sendMessage(Messages.CANNOT_CHANGE_SPAWNER);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerSpawnedMob(SpawnerSpawnEvent event) {
        CreatureSpawner creatureSpawner = event.getSpawner();
        if (!SpawnerState.isValid(creatureSpawner)) {
            return;
        }
        SpawnerState spawner = SpawnerState.from(creatureSpawner);

        if (!spawner.isRunning()) {
            event.setCancelled(true);
            return;
        }
        
        if (spawner.getSpawnableMobs() <= 0) {
            event.setCancelled(true);
        } else {
            spawner.decrementSpawnableMobs();
        }

        creatureSpawner.update();
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerItemClicked(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (SpawnerItem.isValid(item)) {
            event.setCurrentItem(SpawnerItem.from(item).getWithLocale(((Player) event.getWhoClicked()).locale()));
        }
    }
}
