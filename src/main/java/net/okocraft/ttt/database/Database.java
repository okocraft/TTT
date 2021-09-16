package net.okocraft.ttt.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.okocraft.ttt.TTT;
import net.okocraft.ttt.util.Functions.SQLConsumer;
import net.okocraft.ttt.util.Functions.SQLFunction;

public class Database {

    private final TTT plugin;

    /** データベースのコネクションプール。 */
    private final HikariDataSource hikari;

    /** SQLiteかどうか。 */
    private final boolean isSQLite;

    /**
     * 初期設定でSQLiteに接続する。
     * 
     * @param dbPath SQLiteのデータファイルのパス
     * @throws SQLException {@code Connection}の生成中に例外が発生した場合
     */
    public Database(TTT plugin) throws SQLException {
        this.plugin = plugin;

        Path dbPath = plugin.getPluginDirectory().resolve("database.db");
        dbPath.toFile().getParentFile().mkdirs();
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + dbPath.toFile().getPath());
        hikari = new HikariDataSource(config);
        isSQLite = true;
    }

    /**
     * 推奨設定でMySQLに接続する。 参照:
     * https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
     * 
     * @param host     ホスト
     * @param port     ポート
     * @param user     ユーザー
     * @param password パスワード
     * @param dbName   データベースの名前
     * @throws SQLException {@code Connection}の生成中に例外が発生した場合
     */
    public Database(TTT plugin, String host, int port, String user, String password, String dbName)
            throws SQLException {
        this.plugin = plugin;

        HikariConfig config = new HikariConfig();

        // login data
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false");
        config.setUsername(user);
        config.setPassword(password);

        // general mysql settings
        config.setMaxLifetime(600000L);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtsCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("maintainTimeStats", false);
        hikari = new HikariDataSource(config);
        isSQLite = false;
    }

    public boolean isSQLite() {
        return isSQLite;
    }

    /**
     * 指定した {@code sql} から {@link PreparedStatement} を生成し、第二引数で生成した
     * {@link PreparedStatement} の?引数をsetなどして、それをそのまま実行する。
     * 
     * @param sql               実行するSQL文。メソッド内で {@link PreparedStatement} に変換される。
     * @param statementConsumer 生成された {@link PreparedStatement}
     *                          を処理する関数。この処理の後に{@link PreparedStatement#execute()}が実行される。
     * @return sql文の実行に成功したかどうか
     */
    public boolean execute(String sql, SQLConsumer<PreparedStatement> statementConsumer) {
        try (Connection con = hikari.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement(sql);) {
            statementConsumer.accept(preparedStatement);
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().warning("Error occurred on executing SQL: " + sql);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定した {@code sql} から {@link PreparedStatement} を生成し、第二引数で生成した
     * {@link PreparedStatement} の?引数をsetなどして、その結果返ってきた {@link ResultSet}
     * を第三引数で更に処理する。第三引数の処理が終わった後に、ResultSetはクローズされる。
     * 
     * @param sql               実行するSQL文。メソッド内で {@link PreparedStatement} に変換される。
     * @param statementConsumer 生成された {@link PreparedStatement}
     *                          を処理する関数。この処理の後に{@link PreparedStatement#executeQuery()}が実行される。
     * @param function          実行結果を受け取り、型Tの処理結果を返す関数。
     * 
     * @param <T>               {@link ResultSet} を処理した結果の型
     * 
     * @return {@code function} の処理結果
     */
    public <T> T query(String sql, SQLConsumer<PreparedStatement> statementConsumer,
            SQLFunction<ResultSet, T> function) {
        try (Connection con = hikari.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement(sql);) {
            statementConsumer.accept(preparedStatement);
            return function.apply(preparedStatement.executeQuery());
        } catch (SQLException e) {
            plugin.getLogger().warning("Error occurred on executing SQL: " + sql);
            e.printStackTrace();
            return null;
        }
    }

    public Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }

    /**
     * データベースのコネクションプールやコネクションを閉じる。
     */
    public void dispose() {
        if (hikari != null) {
            hikari.close();
        }
    }
}