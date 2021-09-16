package net.okocraft.ttt.module.farm;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.database.Database;

public class EntityDeathLogTable {

    private final TTT plugin;
    private final Database database;

    EntityDeathLogTable(TTT plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();

        String schema = """
                CREATE TABLE IF NOT EXISTS entity_death_logs (
                    timestump BIGINT PRIMARY KEY,
                    entity VARCHAR(63) NOT NULL,
                    spawn_reason VARCHAR(31) NOT NULL,
                    spawn_world_name VARCHAR(63) NOT NULL,
                    spawn_x_location INT NOT NULL,
                    spawn_y_location INT NOT NULL,
                    spawn_z_location INT NOT NULL,
                    death_world_name VARCHAR(63) NOT NULL,
                    death_x_location INT NOT NULL,
                    death_y_location INT NOT NULL,
                    death_z_location INT NOT NULL
                )
                """;

        database.execute(schema, statement -> {});
    }

    public record LogEntity(
        long timestump,
        @NotNull EntityType entity,
        @NotNull SpawnReason spawnReason,
        @NotNull String spawnWorldName,
        int spawnXLocation,
        int spawnYLocation,
        int spawnZLocation,
        @NotNull String deathWorldName,
        int deathXLocation,
        int deathYLocation,
        int deathZLocation
    ) {
        public Optional<World> getSpawnWorld() {
            return Optional.ofNullable(Bukkit.getWorld(spawnWorldName));
        }

        public Optional<World> getDeathWorld() {
            return Optional.ofNullable(Bukkit.getWorld(deathWorldName));
        }
    }

    public boolean insert(LogEntity log) {
        database.execute("""
                INSERT INTO entity_death_logs (
                    timestump, 
                    entity, 
                    spawn_reason, 
                    spawn_world_name, 
                    spawn_x_location, 
                    spawn_y_location, 
                    spawn_z_location, 
                    death_world_name, 
                    death_x_location, 
                    death_y_location, 
                    death_z_location
                ) VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                """,
                statement -> {
                    statement.setLong(1, log.timestump);
                    statement.setString(2, log.entity.name());
                    statement.setString(3, log.spawnReason.name());
                    statement.setString(4, log.spawnWorldName);
                    statement.setInt(5, log.spawnXLocation);
                    statement.setInt(6, log.spawnYLocation);
                    statement.setInt(7, log.spawnZLocation);
                    statement.setString(8, log.deathWorldName);
                    statement.setInt(9, log.deathXLocation);
                    statement.setInt(10, log.deathYLocation);
                    statement.setInt(11, log.deathZLocation);
                }
        );
        return true;
    }

    public static class Field<T, K> {
        public static final Field<Long, Long> TIMESTUMP = new Field<>("timestump", false, l -> l);
        public static final Field<EntityType, String> ENTITY = new Field<>("entity", false, EntityType::name);
        public static final Field<SpawnReason, String> SPAWN_REASON = new Field<>("spawn_reason", false, SpawnReason::name);
        public static final Field<World, String> SPAWN_WORLD_NAME = new Field<>("spawn_world_name", false, World::getName);
        public static final Field<Integer, Integer> SPAWN_X_LOCATION = new Field<>("spawn_x_location", false, i -> i);
        public static final Field<Integer, Integer> SPAWN_Y_LOCATION = new Field<>("spawn_y_location", false, i -> i);
        public static final Field<Integer, Integer> SPAWN_Z_LOCATION = new Field<>("spawn_z_location", false, i -> i);
        public static final Field<World, String> DEATH_WORLD_NAME = new Field<>("death_world_name", false, World::getName);
        public static final Field<Integer, Integer> DEATH_X_LOCATION = new Field<>("death_x_location", false, i -> i);
        public static final Field<Integer, Integer> DEATH_Y_LOCATION = new Field<>("death_y_location", false, i -> i);
        public static final Field<Integer, Integer> DEATH_Z_LOCATION = new Field<>("death_z_location", false, i -> i);

        public final String databaseName;
        public final boolean nullable;
        private final Function<T, K> typeConverter;

        private Field(String databaseName, boolean nullable, Function<T, K> typeConverter) {
            this.databaseName = databaseName;
            this.nullable = nullable;
            this.typeConverter = typeConverter;
        }

        public K convert(T type) {
            return typeConverter.apply(type);
        }
    }

    public static class Condition implements Cloneable {

        private final StringBuilder sb = new StringBuilder(" WHERE");
        private final Map<Integer, Object> argValues = new HashMap<>();

        private int index = 1;

        /**
         * Constructor to clone.
         * 
         * @param original clone source
         */
        private Condition(Condition original) {
            sb.setLength(0);
            sb.append(original.toString());
            argValues.putAll(original.argValues);
            this.index = original.index;
        }

        public <T> Condition(@NotNull Field<T, ?> field, @NotNull T value) {
            addCondition(field, value);
        }

        public <T> Condition and(@NotNull Field<T, ?> field, @NotNull T value) {
            sb.append(" AND");
            addCondition(field, value);
            return this;
        }

        public <T extends Comparable<T>> Condition and(@NotNull Field<T, ?> field, @NotNull T min, @NotNull T max) {
            sb.append(" AND");
            addCondition(field, min, max);
            return this;
        }
        
        public <T> Condition or(@NotNull Field<T, ?> field, @NotNull T value) {
            sb.append(" OR");
            addCondition(field, value);
            return this;
        }
        
        public <T extends Comparable<T>> Condition or(@NotNull Field<T, ?> field, @NotNull T min, @NotNull T max) {
            sb.append(" OR");
            addCondition(field, min, max);
            return this;
        }

        /**
         * sbで管理されているwhere文の末尾に新たな条件を付加する。
         * 
         * @param <T> フィールドの型
         * @param field データベースのカラム
         * @param value カラムの値
         */
        private <T> void addCondition(@NotNull Field<T, ?> field, @NotNull T value) {
            sb.append(" ").append(field.databaseName).append(" = ?");
            argValues.put(index++, field.convert(value));
        }

        /**
         * sbで管理されているwhere文の末尾に新たな条件を付加する。
         * 
         * @param <T> 比較可能なフィールドの型
         * @param field 比較可能な型を持つ、データベースのカラム
         * @param min カラムの最小値
         * @param max カラムの最大値
         */
        private <T extends Comparable<T>> void addCondition(@NotNull Field<T, ?> field, @NotNull T min, @NotNull T max) {
            sb.append(" ").append(field.databaseName).append(" BETWEEN ? AND ?");
            argValues.put(index++, field.convert(min));
            argValues.put(index++, field.convert(max));
        }

        private String generateWhere() {
            return sb.toString();
        }

        private void accept(PreparedStatement statement) throws SQLException {
            for (Map.Entry<Integer, Object> entry : argValues.entrySet()) {
                statement.setObject(entry.getKey(), entry.getValue());
            }
        }

        @Override
        protected Condition clone() {
            return new Condition(this);
        }
    }

    public List<LogEntity> search(Condition condition) {
        return database.query(
                "SELECT * FROM entity_death_logs" + condition.generateWhere(),
                condition::accept,
                resultSet -> {
                    List<LogEntity> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(new LogEntity(
                                resultSet.getLong(Field.TIMESTUMP.databaseName),
                                EntityType.valueOf(resultSet.getString(Field.ENTITY.databaseName)),
                                SpawnReason.valueOf(resultSet.getString(Field.SPAWN_WORLD_NAME.databaseName)),
                                resultSet.getString(Field.SPAWN_WORLD_NAME.databaseName),
                                resultSet.getInt(Field.SPAWN_X_LOCATION.databaseName),
                                resultSet.getInt(Field.SPAWN_Y_LOCATION.databaseName),
                                resultSet.getInt(Field.SPAWN_Z_LOCATION.databaseName),
                                resultSet.getString(Field.DEATH_WORLD_NAME.databaseName),
                                resultSet.getInt(Field.DEATH_X_LOCATION.databaseName),
                                resultSet.getInt(Field.DEATH_Y_LOCATION.databaseName),
                                resultSet.getInt(Field.DEATH_Z_LOCATION.databaseName)
                        ));
                    }
                    return result;
                }
        );
    }
}
