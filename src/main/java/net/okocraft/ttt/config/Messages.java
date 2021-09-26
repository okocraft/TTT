  
package net.okocraft.ttt.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.okocraft.ttt.module.farm.EntityDeathLogTable.LogEntity;
import net.okocraft.ttt.module.spawner.SpawnerItem;
import net.okocraft.ttt.module.spawner.SpawnerState;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;

public final class Messages {

    private static final TranslatableComponent PREFIX = translatable("plugin-prefix", GRAY);

    public static final Component COMMAND_NO_PERMISSION =
            PREFIX.toBuilder().append(translatable("command.no-permission", RED)).build();

    public static final Component COMMAND_NOT_ENOUGH_ARGUMENTS =
            PREFIX.toBuilder().append(translatable("command.not-enough-arguments", RED)).build();

    public static final Component COMMAND_INVALID_ENTITY_TYPE =
            PREFIX.toBuilder().append(translatable("command.invalid-entity-type", RED)).build();

    public static final Component COMMAND_PLAYER_ONLY =
            PREFIX.toBuilder().append(translatable("command.player-only", RED)).build();

    public static final Component COMMAND_NEAR_HEADER =
            PREFIX.toBuilder().append(translatable("command.player-only", RED)).build();

    public static final Function<CreatureSpawner, Component> COMMAND_NEAR_ENTRY =
            spawner -> {
                Location loc = spawner.getLocation();
                String locStr = loc.getWorld().getName() + "/" + loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ();
            
                return translatable()
                        .key("too-many-spawners")
                        .args(translatable(spawner.getType(), AQUA), text(locStr, AQUA))
                        .color(RED)
                        .build();
            };

    public static final BiFunction<Integer, Integer, Component> TOO_MANY_SPAWNERS =
            (range, amount) -> PREFIX.toBuilder()
                    .append(
                            translatable()
                                    .key("too-many-spawners")
                                    .args(text(range, AQUA), text(amount, AQUA))
                                    .color(RED)
                                    .build()
                    )
                    .build();

    public static final Function<SpawnerState, Component> SHOWN_SPAWNER_STATUS =
            spawner -> PREFIX.toBuilder()
                    .append(
                            translatable()
                                    .key("shown-spawner-status")
                                    .args(
                                            text(spawner.isRunning(), AQUA),
                                            translatable(spawner.getSpawnedType(), AQUA),
                                            text(spawner.getSpawnableMobs(), AQUA),
                                            text(spawner.getMaxSpawnableMobs(), AQUA))
                                    .color(GRAY)
                                    .build()
                    )
                    .build();

    public static final Component SPAWNER_START_TIP =
            PREFIX.toBuilder().append(translatable("spawner-start-tip", GRAY)).build();

    public static final Component SPAWNER_START_TIP_REVERSE =
            PREFIX.toBuilder().append(translatable("spawner-start-tip-reverse", GRAY)).build();

    public static final Component CANNOT_CHANGE_SPAWNER =
            PREFIX.toBuilder().append(translatable("cannot-change-spawner", GRAY)).build();

    public static final Component PLAYER_NOT_FOUND =
            PREFIX.toBuilder().append(translatable("player-not-found", RED)).build();

    public static final Component PLAYER_LIMIT_RESET =
            PREFIX.toBuilder().append(translatable("player-limit-reset", GRAY)).build();

    public static final Function<EntityType, Component> SPAWNER_DISPLAY_NAME =
            entityType -> translatable()
                    .key("spawner-item.display-name")
                    .args(translatable(entityType))
                    .color(LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, State.FALSE)
                    .build();

    public static final Function<LogEntity, Component> FARM_IS_DETECTED =
            log -> translatable()
                    .key("farm-is-detected")
                    .args(translatable(log.entity()), text(
                            log.deathWorldName() + "/" +
                            log.deathXLocation() + "/" +
                            log.deathYLocation() + "/" +
                            log.deathZLocation()
                    ))
                    .color(GRAY)
                    .decoration(TextDecoration.ITALIC, State.FALSE)
                    .clickEvent(ClickEvent.suggestCommand("/tp " + log.deathXLocation() + " " + log.deathYLocation() + " " + log.deathZLocation()))
                    .hoverEvent(HoverEvent.showText(text("/tp " + log.deathXLocation() + " " + log.deathYLocation() + " " + log.deathZLocation())))
                    .build();

    public static final Function<SpawnerItem, Component> SPAWNER_LORE =
            spawner -> translatable()
                    .key("spawner-item.lore")
                    .args(text(spawner.getSpawnableMobs()), text(spawner.getMaxSpawnableMobs()))
                    .color(GRAY)
                    .decoration(TextDecoration.ITALIC, State.FALSE)
                    .build();

    private Messages() {
        throw new UnsupportedOperationException();
    }
}