package net.okocraft.ttt.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.language.Messages;

public class ReloadCommand extends AbstractCommand {

    private final TTT plugin;

    public ReloadCommand(TTT plugin) {
        super("reload", "ttt.command.spawner.reload", Set.of("r"));
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkPermission(sender)) {
            sender.sendMessage(Messages.COMMAND_NO_PERMISSION);
            return;
        }
        if (args.length == 2) {
            sender.sendMessage(Messages.COMMAND_NOT_ENOUGH_ARGUMENTS);
            return;
        }

        plugin.reload();
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
