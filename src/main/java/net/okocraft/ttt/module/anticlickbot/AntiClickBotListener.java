package net.okocraft.ttt.module.anticlickbot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.worldsetting.anticlickbot.AntiClickBotSetting;
import net.okocraft.ttt.language.Messages;

public class AntiClickBotListener implements Listener {

    private static final Map<UUID, String> WAITING_VERIFICATION = new HashMap<>();
    private static final Map<UUID, KillLog> KILL_LOGS = new HashMap<>();
    
    private final TTT plugin;

    public AntiClickBotListener(TTT plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) {
            return;
        }
        if (WAITING_VERIFICATION.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        
        KillLog log = KILL_LOGS.computeIfAbsent(
                player.getUniqueId(),
                uid -> new KillLog(1, player.getLocation(), System.currentTimeMillis())
        );

        AntiClickBotSetting setting = plugin.getSetting().worldSetting(player.getWorld()).antiClickBotSetting();
        int distanceThreshold = setting.distanceThreshold();
        if (log.killCount() > 1) {
            if (log.location().distanceSquared(player.getLocation()) > distanceThreshold * distanceThreshold) {
                log.killCount(0);
            }
            log.killCount(log.killCount() + 1);
            log.location(player.getLocation());
        }

        if (log.killCount() > setting.killCountThreshold()) {
            String randomString = randomAlphanumeric(3);
            WAITING_VERIFICATION.put(player.getUniqueId(), randomString);
            player.sendMessage(Messages.VERIFY_CLICK_BOT.apply(randomString, setting.verificationTimeout()));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!WAITING_VERIFICATION.containsKey(player.getUniqueId())) {
                        return;
                    }

                    switch (setting.punishment()) {
                    case BAN:
                        player.banPlayer("Due to failure click bot verification. Please ask to admin in discord: https://discord.gg/7zYUbYPv");
                        break;
                    case KICK:
                        player.kick(Component.text("Due to failure click bot verification."), Cause.PLUGIN);
                        break;
                    case KILL:
                        player.setHealth(0);
                        break;
                    default:
                        boolean success = false;
                        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 64, 64, 64)) {
                            if (entity instanceof Monster) {
                                if (!entity.hasGravity() || entity.getCustomName() != null || !entity.isDead() || !entity.isGlowing() || !entity.isInsideVehicle() || !entity.isInvulnerable() || !entity.isSilent()) {
                                    continue;
                                }

                                if (entity.teleport(player)) {
                                    success = true;
                                    break;
                                }
                            }
                        }
                        if (!success) {
                            player.setHealth(0);
                        }
                        break;
                    }

                    WAITING_VERIFICATION.remove(player.getUniqueId());
                    KILL_LOGS.remove(player.getUniqueId());

                }
            }.runTaskLater(plugin, 20L * setting.verificationTimeout());
        }
    }

    @EventHandler
    private void onEntityDamaged(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Player player)) {
            return;
        }

        if (!getVerificationString(player.getUniqueId()).isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        UUID playerUid = event.getPlayer().getUniqueId();
        WAITING_VERIFICATION.remove(playerUid);
        KILL_LOGS.remove(playerUid);
    }

    public static String getVerificationString(UUID playerUid) {
        return WAITING_VERIFICATION.getOrDefault(playerUid, "");
    }

    public static boolean inputVerification(UUID playerUid, String verificationString) {
        if (verificationString == null || verificationString.isEmpty()) {
            return false;
        }
        if (!getVerificationString(playerUid).equals(verificationString)) {
            return false;
        }

        WAITING_VERIFICATION.remove(playerUid);
        KILL_LOGS.remove(playerUid);
        return true;
    }

    private static final Random RANDOM = new Random();
    private static String randomAlphanumeric(int count) {
        char[] result = new char[count];
        for (int i = 0; i < count; i++) {
            int randomInt = RANDOM.nextInt(36);
            if (randomInt < 10) {
                result[i] = (char) (48 + randomInt);
            } else {
                result[i] = (char) (97 + (randomInt - 10));
            }
        }
        return new String(result);
    }
}
