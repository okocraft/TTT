  
package net.okocraft.ttt.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Objects;
import java.util.function.Function;

import org.bukkit.entity.EntityType;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;

public final class Messages {

    private static final TranslatableComponent PREFIX = translatable("plugin-prefix", GRAY);

    public static final Component COMMAND_NOT_ENOUGH_ARGUMENTS =
            PREFIX.toBuilder().append(translatable("command.not-enough-arguments", RED)).build();

    public static final Component COMMAND_INVALID_ENTITY_TYPE =
            PREFIX.toBuilder().append(translatable("command.invalid-entity-type", RED)).build();

    public static final Component COMMAND_PLAYER_ONLY =
            PREFIX.toBuilder().append(translatable("command.player-only", RED)).build();

    public static final QuadFunction<Boolean, EntityType, Integer, Integer, Component> SHOWN_SPAWNER_STATUS =
            (running, type, limit, maxLimit) -> PREFIX.toBuilder()
                    .append(
                            translatable()
                                    .key("shown-spawner-status")
                                    .args(
                                            text(running, AQUA),
                                            translatable(type, AQUA),
                                            text(limit, AQUA),
                                            text(maxLimit, AQUA))
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

    public static final Function<EntityType, Component> SPAWNER_DISPLAY_NAME =
            entityType -> PREFIX.toBuilder()
                    .append(
                            translatable()
                                    .key("spawner-item.display-name")
                                    .args(translatable(entityType, AQUA))
                                    .color(LIGHT_PURPLE)
                                    .build()
                    )
                    .build();

    public static final TriFunction<Integer, Integer, Integer, Component> SPAWNER_LORE =
            (index, limit, limitMax) -> PREFIX.toBuilder()
                    .append(
                            translatable()
                                    .key("spawner-item.lore" + index)
                                    .args(text(limit), text(limitMax))
                                    .color(GRAY)
                                    .build()
                    )
                    .build();

    private Messages() {
        throw new UnsupportedOperationException();
    }

    @FunctionalInterface
    public interface TriFunction<A,B,C,R> {

        R apply(A a, B b, C c);
    
        default <V> TriFunction<A, B, C, V> andThen(
                                    Function<? super R, ? extends V> after) {
            Objects.requireNonNull(after);
            return (A a, B b, C c) -> after.apply(apply(a, b, c));
        }
    }

    @FunctionalInterface
    public interface QuadFunction<A,B,C,D,R> {

        R apply(A a, B b, C c, D d);
    
        default <V> QuadFunction<A, B, C, D, V> andThen(
                                    Function<? super R, ? extends V> after) {
            Objects.requireNonNull(after);
            return (A a, B b, C c, D d) -> after.apply(apply(a, b, c, d));
        }
    }
}