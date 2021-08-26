package net.okocraft.ttt.command;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.config.Messages;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class TTTCommand extends AbstractCommand implements CommandExecutor, TabCompleter {

    private final List<AbstractCommand> subCommands;

    private TTTCommand(@NotNull TTT plugin) { // for subcommand
        super("ttt", "ttt.command", Collections.emptySet());
        this.subCommands = List.of(
            new GetSpawner(plugin)
        );
    }

    public static void register(@NotNull TTT plugin, @NotNull PluginCommand command) {
        var implementation = new TTTCommand(plugin);

        command.setExecutor(implementation);
        command.setTabCompleter(implementation);
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkPermission(sender)) {
            sender.sendMessage(Messages.COMMAND_NO_PERMISSION);
            return;
        }

        if (args.length == 0) {
            // TODO send help
            return;
        }

        var subCommand = search(args[0]);

        if (subCommand.isPresent()) {
            subCommand.get().onCommand(sender, args);
        } else {
            // TODO send subcommand-not-found message
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0 || !sender.hasPermission(getPermission())) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(
                    args[0],
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

        return search(args[0])
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

    // The command is implemented at onCommand(CommandSender, String[]).
    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                   @NotNull String label, @NotNull String[] args) {
        onCommand(sender, args);
        return true;
    }

    // The tab completion is implemented at onTabComplete(CommandSender, String[]).
    @Override
    public final @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                     @NotNull String label, @NotNull String[] args) {
        return onTabComplete(sender, args);
    }
}
