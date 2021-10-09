package net.okocraft.ttt.command.spawner;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.command.AbstractCommand;
import net.okocraft.ttt.config.Messages;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class SpawnerCommand extends AbstractCommand {

    private final List<AbstractCommand> subCommands;

    public SpawnerCommand(@NotNull TTT plugin) {
        super("spawner", "ttt.command.spawner", Set.of("s"));
        this.subCommands = List.of(
            new GetCommand(plugin),
            new NearCommand(plugin),
            new ResetLimitCommand(plugin)
        );
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkPermission(sender)) {
            sender.sendMessage(Messages.COMMAND_NO_PERMISSION);
            return;
        }

        if (args.length == 1) {
            // TODO send help
            return;
        }

        var subCommand = search(args[1]);

        if (subCommand.isPresent()) {
            subCommand.get().onCommand(sender, args);
        } else {
            // TODO send subcommand-not-found message
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1 || !sender.hasPermission(getPermission())) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(
                    args[1],
                    subCommands.stream()
                            .map(command -> {
                                Set<String> canditates = new HashSet<>();
                                canditates.add(command.getName());
                                canditates.addAll(command.getAliases());
                                return canditates;
                            })
                            .flatMap(Set::stream)
                            .toList(),
                    new ArrayList<>()
            );
        }

        return search(args[1])
                .map(cmd -> cmd.onTabComplete(sender, args))
                .orElse(Collections.emptyList());
    }

    private @NotNull Optional<AbstractCommand> search(@NotNull String name) {
        name = name.toLowerCase(Locale.ROOT);

        for (var subCommand : subCommands) {
            if (subCommand.getName().equals(name) || subCommand.getAliases().contains(name)) {
                return Optional.of(subCommand);
            }
        }

        return Optional.empty();
    }
}
