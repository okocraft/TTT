package net.okocraft.ttt.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.github.siroshun09.configapi.api.Configuration;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.Messages;

public class ResetLimit extends AbstractCommand {

    private final TTT plugin;

    public ResetLimit(TTT plugin) {
        super("resetlimit", "ttt.command.resetlimit", Set.of("rl"));
        this.plugin = plugin;
        // /ttt resetlimit <player> [world] [mobtype]
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkPermission(sender)) {
            sender.sendMessage(Messages.COMMAND_NO_PERMISSION);
            return;
        }
        if (args.length == 1) {
            sender.sendMessage(Messages.COMMAND_NOT_ENOUGH_ARGUMENTS);
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[1]);
        Configuration playerData = plugin.getPlayerData();
        if (!playerData.getKeyList().contains(player.getUniqueId().toString())) {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND);
            return;
        }

        if (args.length == 2) {
            playerData.set(player.getUniqueId().toString(), null);
            sender.sendMessage(Messages.PLAYER_LIMIT_RESET);
            return;
        }
        
        Configuration data = playerData.getSection(player.getUniqueId().toString());
        String worldName = args[2];
        if (args.length == 3) {
            data.set(worldName, null);
            sender.sendMessage(Messages.PLAYER_LIMIT_RESET);
            return;
        }

        EntityType type;
        try {
            type = EntityType.valueOf(args[3].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Messages.COMMAND_INVALID_ENTITY_TYPE);
            return;
        }

        data.set(worldName + "." + type.name(), null);
        // TODO: player limit data reset msg.
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        Configuration playerData = plugin.getPlayerData();
        List<String> players = playerData.getKeyList().stream()
                .map(UUID::fromString)
                .map(plugin.getServer()::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .toList();
        
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], players, new ArrayList<>());
        }

        if (!players.contains(args[1])) {
            return new ArrayList<>();
        }

        UUID uuid = plugin.getServer().getOfflinePlayer(args[1]).getUniqueId();

        List<String> worlds = playerData.getStringList(uuid.toString());
        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], worlds, new ArrayList<>());
        }

        if (!worlds.contains(args[2])) {
            return new ArrayList<>();
        }

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], playerData.getSection(uuid.toString() + "." + args[2]).getKeyList(), new ArrayList<>());
        }
        return new ArrayList<>();
    }
}