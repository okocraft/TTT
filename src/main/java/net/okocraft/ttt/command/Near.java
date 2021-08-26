package net.okocraft.ttt.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.Messages;
import net.okocraft.ttt.config.Settings;

public class Near extends AbstractCommand {

    private final TTT plugin;

    public Near(TTT plugin) {
        super("near", "ttt.command.near");
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkPermission(sender)) {
            sender.sendMessage(Messages.COMMAND_NO_PERMISSION);
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.COMMAND_PLAYER_ONLY);
            return;
        }

        player.sendMessage(Messages.COMMAND_NEAR_HEADER);

        int radius;
        if (args.length >= 2) {
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                radius = plugin.getConfiguration().get(Settings.SPAWNER_ISOLATING_RADIUS);
                
            }
        } else {
            radius = plugin.getConfiguration().get(Settings.SPAWNER_ISOLATING_RADIUS);
        }
        
        for (CreatureSpawner spawner : plugin.getSpawnerUtil().getSpawnersIn(radius, player.getLocation())) {
            player.sendMessage(Messages.COMMAND_NEAR_ENTRY.apply(spawner));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(
                    args[1],
                    IntStream.rangeClosed(1, 34).mapToObj(Integer::toString).toList(),
                    new ArrayList<>()
            );
        }

        return new ArrayList<>();
    }
}
