package net.okocraft.ttt.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.language.Messages;
import net.okocraft.ttt.module.anticlickbot.AntiClickBotListener;

public class VerifyCommand extends AbstractCommand {

    public VerifyCommand(TTT plugin) {
        super("verify", "ttt.command.verify", Set.of("v"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkPermission(sender)) {
            sender.sendMessage(Messages.COMMAND_NO_PERMISSION);
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Messages.COMMAND_NOT_ENOUGH_ARGUMENTS);
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.COMMAND_PLAYER_ONLY);
            return;
        }
        
        if (AntiClickBotListener.inputVerification(((Player) sender).getUniqueId(), args[1].toLowerCase(Locale.ROOT))) {
            sender.sendMessage(Messages.COMMAND_VERIFY_SUCCESS);
        } else {
            sender.sendMessage(Messages.COMMAND_VERIFY_FAIL);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
