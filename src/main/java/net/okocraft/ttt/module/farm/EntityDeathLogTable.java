package net.okocraft.ttt.module.farm;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;

import net.okocraft.ttt.database.Database;

public class EntityDeathLogTable {

    private final Database database;

    EntityDeathLogTable(Database database) {
        this.database = database;

        String schema = """
                CREATE TABLE IF NOT EXISTS entity_death_logs (
                    timestamp_milli INT NOT NULL,
                    timestamp_nano BIGINT NOT NULL,
                    entity VARCHAR(63) NOT NULL,
                    spawn_reason VARCHAR(31) NOT NULL,
                    spawn_world_name VARCHAR(63) NOT NULL,
                    spawn_x_location INT NOT NULL,
                    spawn_y_location INT NOT NULL,
                    spawn_z_location INT NOT NULL,
                    death_world_name VARCHAR(63) NOT NULL,
                    death_x_location INT NOT NULL,
                    death_y_location INT NOT NULL,
                    death_z_location INT NOT NULL,
                    PRIMARY KEY(timestamp_milli, timestamp_nano)
                )
                """;
        
        database.execute(schema, statement -> {});

        // TODO: mysql index is not supported now.
        if (database.isSQLite()) {
            database.execute("CREATE INDEX IF NOT EXISTS spawn_reason ON entity_death_logs (spawn_reason)", statement -> {});
            database.execute("CREATE INDEX IF NOT EXISTS spawn_world_name ON entity_death_logs (spawn_world_name)", statement -> {});
            database.execute("CREATE INDEX IF NOT EXISTS spawn_location ON entity_death_logs (spawn_x_location, spawn_z_location)", statement -> {});
            database.execute("CREATE INDEX IF NOT EXISTS death_world_name ON entity_death_logs (death_world_name)", statement -> {});
            database.execute("CREATE INDEX IF NOT EXISTS death_location ON entity_death_logs (death_x_location, death_z_location)", statement -> {});
        }

        // delete logs before 3 day
        database.execute(
                "DELETE FROM entity_death_logs WHERE timestamp_milli < ?",
                statement -> statement.setLong(1, System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 3))
        );
        if (database.isSQLite()) {
            database.execute("VACUUM", statement -> {});
        }
        
    }

    public record LogEntity(
        long timestampMilli,
        int timestampNano,
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

        public Location getSpawnLocation() {
            return new Location(Bukkit.getWorld(spawnWorldName), spawnXLocation, spawnYLocation, spawnZLocation);
        }

        public Location getDeathLocation() {
            return new Location(Bukkit.getWorld(deathWorldName), deathXLocation, deathYLocation, deathZLocation);
        }

        public Timestamp getTimestamp() {
            Timestamp timestamp = new Timestamp(timestampMilli);
            timestamp.setNanos(timestampNano % 1000000000);
            return timestamp;
        }
    }

    public boolean insert(LogEntity log) {
        database.execute("""
                INSERT INTO entity_death_logs (
                    timestamp_milli, 
                    timestamp_nano, 
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
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                """,
                statement -> {
                    statement.setLong(1, log.timestampMilli);
                    statement.setInt(2, log.timestampNano);
                    statement.setString(3, log.entity.name());
                    statement.setString(4, log.spawnReason.name());
                    statement.setString(5, log.spawnWorldName);
                    statement.setInt(6, log.spawnXLocation);
                    statement.setInt(7, log.spawnYLocation);
                    statement.setInt(8, log.spawnZLocation);
                    statement.setString(9, log.deathWorldName);
                    statement.setInt(10, log.deathXLocation);
                    statement.setInt(11, log.deathYLocation);
                    statement.setInt(12, log.deathZLocation);
                }
        );
        return true;
    }

    public static class Field<T, K> {
        public static final Field<Long, Long> TIMESTUMP_MILLI = new Field<>("timestamp_milli", false, l -> l);
        public static final Field<Integer, Integer> TIMESTUMP_NANO = new Field<>("timestamp_nano", false, i -> i);
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
                                resultSet.getLong(Field.TIMESTUMP_MILLI.databaseName),
                                resultSet.getInt(Field.TIMESTUMP_NANO.databaseName),
                                EntityType.valueOf(resultSet.getString(Field.ENTITY.databaseName)),
                                SpawnReason.valueOf(resultSet.getString(Field.SPAWN_REASON.databaseName)),
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
