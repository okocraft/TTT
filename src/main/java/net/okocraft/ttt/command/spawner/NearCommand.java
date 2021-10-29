package net.okocraft.ttt.command.spawner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.command.AbstractCommand;
import net.okocraft.ttt.language.Messages;
import net.okocraft.ttt.module.spawner.SpawnerState;

public class NearCommand extends AbstractCommand {

    private final TTT plugin;

    public NearCommand(TTT plugin) {
        super("near", "ttt.command.spawner.near", Set.of("n"));
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

        int defaultRadius = plugin.getSetting()
                .worldSetting(player.getWorld())
                .spawnerSetting()
                .isolatingSetting()
                .radius();
        int radius;
        if (args.length >= 3) {
            try {
                radius = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                radius = defaultRadius;
            }
        } else {
            radius = defaultRadius;
        }
        
        for (CreatureSpawner spawner : SpawnerState.getSpawnersIn(radius, player.getLocation())) {
            player.sendMessage(Messages.COMMAND_NEAR_ENTRY.apply(spawner));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 3) {
            return StringUtil.copyPartialMatches(
                    args[2],
                    IntStream.rangeClosed(1, 34).mapToObj(Integer::toString).toList(),
                    new ArrayList<>()
            );
        }

        return new ArrayList<>();
    }
}
