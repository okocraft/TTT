package net.okocraft.ttt.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractCommand {

    private final String name;
    private final String permission;
    private final Set<String> aliases;

    public AbstractCommand(@NotNull String name, @NotNull String permission) {
        this(name, permission, Collections.emptySet());
    }

    public AbstractCommand(@NotNull String name, @NotNull String permission, @NotNull Set<String> aliases) {
        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getPermission() {
        return permission;
    }

    public @NotNull @Unmodifiable Set<String> getAliases() {
        return aliases;
    }

    public abstract void onCommand(@NotNull CommandSender sender, @NotNull String[] args);

    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }

    /**
     * Check if the executor has permissions.
     * <p>
     * If the executor has no permission, send a no-permission message.
     *
     * @param target the {@link CommandSender} to check
     * @return {@code true} if the user has permission, {@code false} otherwise
     */
    protected boolean checkPermission(@NotNull CommandSender target) {
        if (target.hasPermission(permission)) {
            return true;
        } else {
            // TODO: no-permission message
            return false;
        }
    }
}
