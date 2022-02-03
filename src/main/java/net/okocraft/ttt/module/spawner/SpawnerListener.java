package net.okocraft.ttt.module.spawner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.github.siroshun09.configapi.api.Configuration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.worldsetting.spawner.IsolatingSetting;
import net.okocraft.ttt.config.worldsetting.spawner.RedstoneSwitchesSetting;
import net.okocraft.ttt.language.Messages;

public class SpawnerListener implements Listener {

    private final TTT plugin;

    public SpawnerListener(TTT plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (!(block.getState() instanceof CreatureSpawner state)) {
            return;
        }

        if (!SpawnerState.isValid(state)) {
            if (!player.hasPermission("ttt.spawner.break.*") && !player.hasPermission("ttt.spawner.break." + state.getSpawnedType().getKey().getKey())) {
                event.setCancelled(true);
                return;
            }

            // TODO: temp code
            PersistentDataContainer container = state.getPersistentDataContainer();
            for (NamespacedKey key : container.getKeys()) {
                if (key.getNamespace().equals(plugin.getName().toLowerCase(Locale.ROOT))) {
                    container.remove(key);
                }
            }

            var spawnerSetting = plugin.getSetting().worldSetting(block.getWorld()).spawnerSetting();
            boolean minableSpawnerLimitEnabled = spawnerSetting.minableSpawnerLimitEnabled();
            // mined spawner amount limit
            int minedSpawners = 0;
            if (minableSpawnerLimitEnabled && !player.hasPermission("ttt.bypass.spawner.minelimit")) {
                Configuration playerData = plugin.getPlayerData()
                        .getSection(player.getUniqueId() + ".mined-spawners." + block.getWorld().getUID());
                if (playerData != null) {
                    minedSpawners = playerData.getInteger(state.getSpawnedType().name());

                    int limit = spawnerSetting.maxMinableSpawners(state.getSpawnedType());

                    if (minedSpawners >= limit) {
                        TTT.debug("break cancelled due to mine limit");
                        event.setCancelled(true);
                        player.sendMessage(Messages.spawnerMineLimit(block.getWorld(), state.getSpawnedType(), limit));
                        return;
                    }
                }
            }

            if (!player.hasPermission("ttt.spawner.drop.*") && !player.hasPermission("ttt.spawner.drop." + state.getSpawnedType().getKey().getKey())) {
                return;
            }

            if (player.hasPermission("ttt.spawner.drop.withoutsilktouch") || player.getInventory().getItemInMainHand()
                    .getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                event.setExpToDrop(0);
                block.getWorld().dropItemNaturally(block.getLocation(),
                        SpawnerItem.from(state).getWithLocale(event.getPlayer().locale()));

                if (minableSpawnerLimitEnabled) {
                    minedSpawners++;
                    plugin.getPlayerData().set(player.getUniqueId() + ".mined-spawners." + block.getWorld().getUID() + "."
                            + state.getSpawnedType().name(), minedSpawners);
                    try {
                        plugin.getPlayerData().save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (!player.hasPermission("ttt.spawner.drop.*") && !player.hasPermission("ttt.spawner.drop." + state.getSpawnedType().getKey().getKey())) {
                return;
            }

            if (player.hasPermission("ttt.spawner.drop.withoutsilktouch") || player.getInventory().getItemInMainHand()
                    .getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                event.setExpToDrop(0);
                block.getWorld().dropItemNaturally(block.getLocation(),
                        SpawnerItem.from(state).getWithLocale(event.getPlayer().locale()));
            }
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
        // TODO: Temp code.
        SpawnerItem.fixData(item);
        if (!SpawnerItem.isValid(item)) {
            if (!event.getPlayer().hasPermission("ttt.spawner.place.*") && !event.getPlayer().hasPermission("ttt.spawner.place.pig")) {
                event.setCancelled(true);
            }
            return;
        }

        SpawnerItem spawnerItem = SpawnerItem.from(item);
        if (!event.getPlayer().hasPermission("ttt.spawner.place.*") && !event.getPlayer().hasPermission("ttt.spawner.place." + spawnerItem.getSpawnedType().getKey().getKey())) {
            event.setCancelled(true);
            return;
        }

        SpawnerState.from(spawner).copyFrom(spawnerItem);
        spawner.update();
    }

    private void cancelEventIfNotIsolated(CreatureSpawner placed, BlockPlaceEvent event) {
        IsolatingSetting isolating = plugin.getSetting().worldSetting(placed.getWorld()).spawnerSetting().isolatingSetting();
        if (!isolating.enabled()) {
            return;
        }

        // PlotSquared
        try {
            com.plotsquared.core.plot.Plot plot =
                    com.plotsquared.core.plot.Plot.getPlot(com.plotsquared.bukkit.util.BukkitUtil.adapt(placed.getLocation()));
            if (plot != null) {
                Set<CreatureSpawner> spawners = new HashSet<>();
                for (CreatureSpawner spawner : SpawnerState.getSpawnersIn(
                        isolating.plotsquaredRadius(), placed.getLocation())) {
                    for (com.sk89q.worldedit.regions.CuboidRegion region : plot.getRegions()) {
                        if (region.contains(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(spawner.getLocation())
                                .toVector().toBlockPoint())) {
                            spawners.add(spawner);
                        }
                    }
                }

                int maxSpawners = isolating.plotsquaredAmount();
                if (spawners.size() > maxSpawners) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Messages.TOO_MANY_SPAWNERS_IN_PLOT.apply(maxSpawners));
                    return;
                }
            }
        } catch (NoClassDefFoundError ignored) {
        }

        int maxSpawners = isolating.amount();
        int radius = isolating.radius();

        if (SpawnerState.getSpawnersIn(radius, placed.getLocation()).size() > maxSpawners) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Messages.TOO_MANY_SPAWNERS.apply(radius, maxSpawners));
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

        RedstoneSwitchesSetting redstoneSwitches = plugin.getSetting().worldSetting(spawner.getWorld())
                .spawnerSetting().redstoneSwitchesSetting();

        if (redstoneSwitches.enabled()) {
            if (redstoneSwitches.reversed()) {
                event.getPlayer().sendMessage(Messages.SPAWNER_START_TIP);
            } else {
                event.getPlayer().sendMessage(Messages.SPAWNER_STOP_TIP);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void checkSpawnEggAndCancel(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        ItemMeta meta = event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
        if (clickedBlock != null && clickedBlock.getType() == Material.SPAWNER && meta instanceof SpawnEggMeta eggMeta
                && (event.getPlayer().hasPermission("ttt.spawner.change.*")
                        || event.getPlayer().hasPermission("ttt.spawner.change." + eggMeta.getSpawnedType()))) {
            event.setUseItemInHand(Result.DENY);
            event.getPlayer().sendMessage(Messages.CANNOT_CHANGE_SPAWNER);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(b -> b.getType() == Material.SPAWNER);
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(b -> b.getType() == Material.SPAWNER);
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

        /* okocraft ancient - disable max-spawnable-mobs-limit
        if (spawner.getSpawnableMobs() <= 0) {
            event.setCancelled(true);
        } else {
            spawner.decrementSpawnableMobs();
        }

        creatureSpawner.update();
        */
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerItemClicked(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (SpawnerItem.isValid(item)) {
            event.setCurrentItem(SpawnerItem.from(item).getWithLocale(((Player) event.getWhoClicked()).locale()));
        }
    }

    @EventHandler
    private void onChunkPopulate(ChunkPopulateEvent event) {
        var spawnerSetting = plugin.getSetting().worldSetting(event.getWorld()).spawnerSetting();
        if (!spawnerSetting.typeMappingEnabled()) {
            return;
        }

        new BukkitRunnable(){

            @Override
            public void run() {
                for (BlockState tile : event.getChunk().getTileEntities()) {
                    if (tile instanceof CreatureSpawner spawner) { // okocraft - remove spawner checking due to disable max-spawnable-mobs-limit
                        Map<EntityType, Double> weightMap = spawnerSetting.typeMapping().row(spawner.getSpawnedType());
                        if (!weightMap.isEmpty()) {
                            EntityType entityType = chooseOnWeight(weightMap);
                            TTT.debug("spawner at " + spawner.getLocation() + " has been changed from " + spawner.getSpawnedType() + " to " + entityType);
                            spawner.setSpawnedType(entityType);
                            spawner.update();
                        } else {
                            TTT.debug("spawner at " + spawner.getLocation() + " cancelled weightMap empty.");
                        }
                    }
                }
            }

        }.runTaskLater(plugin, 1);
    }

    public static <T> T chooseOnWeight(Map<T, Double> weightMap) {
        if (weightMap.isEmpty()) {
            throw new RuntimeException("weightMap cannot be empty.");
        }
        double completeWeight = 0.0;
        for (Double weight : weightMap.values()) {
            completeWeight += weight;
        }
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (Map.Entry<T, Double> entry : weightMap.entrySet()) {
            countWeight += entry.getValue();
            if (countWeight >= r)
                return entry.getKey();
        }
        throw new RuntimeException("Should never be shown.");
    }

    // okocraft ancient - add spawner mob limiter & add setting to disable spawner
    private final Map<org.bukkit.World, Set<org.bukkit.util.BoundingBox>> spawnedRegions = new java.util.HashMap<>();
    private boolean taskStarted = false;

    @EventHandler
    public void onMobSpawn(SpawnerSpawnEvent event) {
        if (spawnerDisabled(event.getSpawner().getWorld())) {
            event.setCancelled(true);
            return;
        }

        if (!mobLimiterEnabled(event.getEntity().getWorld())) {
            return;
        }

        var region =  plugin.getWorldGuardAPI().getRegion(event.getLocation());
        if (region != null) {
            spawnedRegions.computeIfAbsent(event.getSpawner().getWorld(), k -> new HashSet<>()).add(region);
        }
        if (!taskStarted) {
            taskStarted = true;
            checkMobTask().runTaskTimer(plugin, 1L, 20 * 20);
        }
    }

    private org.bukkit.scheduler.BukkitRunnable checkMobTask() {
        return new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                plugin.getServer().getWorlds()
                        .stream()
                        .filter(world -> mobLimiterEnabled(world))
                        .forEach(this::checkMobs);
            }

            private void checkMobs(org.bukkit.World world) {
                var regions = spawnedRegions.get(world);

                if (regions == null) {
                    return;
                }

                var mobMap = new java.util.HashMap<org.bukkit.util.BoundingBox, java.util.List<org.bukkit.entity.LivingEntity>>();

                for (var entity : world.getLivingEntities()) {
                    if (!(entity instanceof org.bukkit.entity.Mob)) {
                        continue;
                    }

                    double x = entity.getLocation().getX();
                    double y = entity.getLocation().getY();
                    double z = entity.getLocation().getZ();

                    for (var region : regions) {
                        if (region.contains(x, y, z)) {
                            mobMap.computeIfAbsent(region, k -> new java.util.ArrayList<>()).add(entity);
                            break;
                        }
                    }
                }

                int maxMobs = maxMobs(world);

                for (var mobs : mobMap.values()) {
                    if (maxMobs <= mobs.size()) {
                        for (var mob : mobs) {
                            mob.remove();
                        }
                    }
                }
            }

            private int maxMobs(org.bukkit.World world) {
                var worldName = world.getName();
                var config = plugin.getConfiguration();

                if (config.get("world-setting." + worldName + ".spawner.mob-limit.max-mobs") instanceof Integer amount) {
                    return amount;
                } else {
                    return config.getInteger("world-setting.default.spawner.mob-limit.max-mobs", 100);
                }
            }
        };
    }

    private boolean spawnerDisabled(org.bukkit.World world) {
        var worldName = world.getName();
        var config = plugin.getConfiguration();

        if (config.get("world-setting." + worldName + ".spawner.disable-spawner") instanceof Boolean bool) {
            return bool;
        } else {
            return config.getBoolean("world-setting.default.spawner.disable-spawner");
        }
    }

    private boolean mobLimiterEnabled(org.bukkit.World world) {
        var worldName = world.getName();
        var config = plugin.getConfiguration();

        if (config.get("world-setting." + worldName + ".spawner.mob-limit.enabled") instanceof Boolean bool) {
            return bool;
        } else {
            return config.getBoolean("world-setting.default.spawner.mob-limit.enabled");
        }
    }
}
