package net.okocraft.ttt;

import java.util.logging.Level;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;

public class DiscordMessenger {

    private final TTT plugin;
    private WebhookClient webhook = null;

    public DiscordMessenger(TTT plugin) {
        this.plugin = plugin;
        restart();
    }

    public void send(String message) {
        if (webhook != null) {
            webhook.send(message);
        }
    }

    public void restart() {
        shutdown();

        String url = plugin.getSetting().discordWebhookUrl();
        if (url == null || url.isEmpty()) {
            plugin.getLogger().warning("Discord webhook url is not set in configuration. Discord webhook feature is disabled.");
            return;
        }
        
        try {
            webhook = new WebhookClientBuilder(url)
                    .setThreadFactory(r -> new Thread(r, "TTT-Notification-Thread"))
                    .setWait(true).build();
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void shutdown() {
        if (webhook != null && !webhook.isShutdown()) {
            webhook.close();
        }
    }
}