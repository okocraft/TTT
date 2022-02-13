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
    private final Map<UUID, Long> lastMessageTimeMap = new HashMap<>(); // okocraft ancient - message cooldown
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
        AntiClickBotSetting setting = plugin.getSetting().worldSetting(player.getWorld()).antiClickBotSetting();

        if (!setting.enabled()) {
            return;
        }

        String randomString = getVerificationString(player.getUniqueId());
        if (!randomString.isEmpty()) {
            if (messageCooldown(player, false)) // okocraft ancient - message cooldown
            player.sendMessage(Messages.VERIFY_CLICK_BOT.apply(randomString, setting.verificationTimeout()));
            event.setCancelled(true);
            return;
        }

        KillLog log = KILL_LOGS.computeIfAbsent(
                player.getUniqueId(),
                uid -> new KillLog(1, player.getLocation(), System.currentTimeMillis())
        );

        if (!log.location().getWorld().equals(player.getLocation().getWorld())) {
            return;
        }

        int distanceThreshold = setting.distanceThreshold();
        if (log.location().distanceSquared(player.getLocation()) > distanceThreshold * distanceThreshold) {
            log.killCount(0);
        }
        log.killCount(log.killCount() + 1);
        log.location(player.getLocation());

        if (log.killCount() > setting.killCountThreshold()) {
            randomString = randomAlphanumeric(3);
            WAITING_VERIFICATION.put(player.getUniqueId(), randomString);
            messageCooldown(player, true); // okocraft ancient - message cooldown
            player.sendMessage(Messages.VERIFY_CLICK_BOT.apply(randomString, setting.verificationTimeout()));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (getVerificationString(player.getUniqueId()).isEmpty()) {
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
                            if (entity instanceof Monster monster) {
                                if (!monster.hasGravity() || monster.getCustomName() != null || monster.isDead() || monster.isGlowing() || monster.isInsideVehicle() || monster.isInvulnerable() || monster.isSilent() || !monster.hasAI() || monster.isVisualFire()) {
                                    continue;
                                }

                                if (monster.teleport(player)) {
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

        String randomString = getVerificationString(player.getUniqueId());
        if (!randomString.isEmpty()) {
            if (messageCooldown(player, false)) // okocraft ancient - message cooldown
            player.sendMessage(Messages.VERIFY_CLICK_BOT.apply(randomString, plugin.getSetting().worldSetting(player.getWorld()).antiClickBotSetting().verificationTimeout()));
            event.setCancelled(true);
        } else lastMessageTimeMap.remove(player.getUniqueId()); // okocraft ancient - message cooldown
    }

    // okocraft ancient - message cooldown
    // if true, the message should be sent
    private boolean messageCooldown(Player player, boolean forceUpdate) {
        if (lastMessageTimeMap.getOrDefault(player.getUniqueId(), 0L) + 1000 < System.currentTimeMillis()) {
            lastMessageTimeMap.put(player.getUniqueId(), System.currentTimeMillis());
            return true;
        } else if (forceUpdate) {
            lastMessageTimeMap.put(player.getUniqueId(), System.currentTimeMillis());
        }
        return false;
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
