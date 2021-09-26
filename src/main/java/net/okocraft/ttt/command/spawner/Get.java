package net.okocraft.ttt.command.spawner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.command.AbstractCommand;
import net.okocraft.ttt.config.Messages;
import net.okocraft.ttt.config.Settings;
import net.okocraft.ttt.module.spawner.SpawnerItem;

public class Get extends AbstractCommand {

    private final TTT plugin;

    public Get(TTT plugin) {
        super("get", "ttt.command.spawner.get", Set.of("g"));
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
        if (args.length == 1) {
            sender.sendMessage(Messages.COMMAND_NOT_ENOUGH_ARGUMENTS);
            return;
        }

        EntityType type;
        try {
            type = EntityType.valueOf(args[2].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Messages.COMMAND_INVALID_ENTITY_TYPE);
            return;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException ignored) {}
        }

        int maxSpawnableMobs = Settings.getMaxSpawnableMobs(plugin.getConfiguration(), player.getWorld(), type);
        ItemStack spawner = SpawnerItem.create(type, maxSpawnableMobs, maxSpawnableMobs).getWithLocale(player.locale());
        spawner.setAmount(amount);
        player.getInventory().addItem(spawner);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> entityTypes = Arrays.stream(EntityType.values()).map(EntityType::name).toList();
        
        if (args.length == 3) {
            return StringUtil.copyPartialMatches(
                    args[2],
                    entityTypes,
                    new ArrayList<>()
            );
        }

        if (entityTypes.contains(args[2].toUpperCase(Locale.ROOT))) {
            return new ArrayList<>();
        }

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(
                    args[3],
                    IntStream.rangeClosed(1, 64).mapToObj(Integer::toString).toList(),
                    new ArrayList<>()
            );
        }

        return new ArrayList<>();
    }
}
