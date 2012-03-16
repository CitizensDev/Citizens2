package net.citizensnpcs.api.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DatabaseStorage implements Storage {
    private final Map<String, Table> tables = Maps.newHashMap();
    private final String url, username, password;

    public DatabaseStorage(String driver, String url, String username, String password) throws SQLException {
        url = "jdbc:" + url;
        this.url = url;
        this.username = username;
        this.password = password;
        DatabaseType.match(driver).load();
        load();
    }

    private void createForeignKey(Table from, Table to) {
        String fk = "fk_" + to.name;
        String sql = "ALTER TABLE `" + from.name + "` ADD FOREIGN KEY (`" + fk + "`) REFERENCES " + to.name + "(`"
                + to.name + "_id`)";
        executeSQL(sql);
        from.foreignKeys.put(fk, new ForeignKey(to, fk));
    }

    private Table createTable(String name, int type) {
        String pk = name + "_id";
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + name + "` ( `" + pk + "` ");
        switch (type) {
        case Types.INTEGER:
            sql.append("int NOT NULL");
            break;
        case Types.VARCHAR:
            sql.append("varchar(255) NOT NULL");
            break;
        default:
            throw new IllegalArgumentException("type not supported");
        }
        sql.append("PRIMARY KEY (`" + pk + "`))");
        executeSQL(sql.toString());
        Table table = new Table().setName(name).setPrimaryKey(pk).setPrimaryKeyType(type);
        tables.put(name, table);
        return table;
    }

    private void executeSQL(String... updates) {
        System.out.println(Joiner.on(" ").join(updates));
        Connection conn = getConnection();
        if (conn == null)
            return;
        try {
            for (String sql : updates) {
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.execute();
                stmt.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    private Connection getConnection() {
        try {
            return (username.isEmpty() && password.isEmpty()) ? DriverManager.getConnection(url) : DriverManager
                    .getConnection(url, username, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public DataKey getKey(String root) {
        String[] split = Iterables.toArray(Splitter.on('.').split(root), String.class);
        if (split[0].isEmpty())
            return new DatabaseKey();
        DataKey table = new DatabaseKey(split[0]);
        if (split.length == 1)
            return table;
        for (int i = 1; i < split.length; ++i) {
            table = table.getRelative(split[i]);
        }
        return table;
    }

    @Override
    public void load() {
        Connection conn = getConnection();
        try {
            ResultSet rs = conn.getMetaData().getTables(null, null, null, new String[] { "TABLE" });
            while (rs.next()) {
                tables.put(rs.getString("TABLE_NAME"), new Table());
            }
            rs.close();
            for (Entry<String, Table> entry : tables.entrySet()) {
                entry.getValue().name = entry.getKey();
                rs = conn.getMetaData().getColumns(null, null, entry.getKey(), null);
                while (rs.next()) {
                    entry.getValue().columns.add(rs.getString("COLUMN_NAME"));
                    rs.getMetaData().getColumnType(0);
                }
                rs.close();
                rs = conn.getMetaData().getPrimaryKeys(null, null, entry.getKey());
                while (rs.next()) {
                    entry.getValue().primaryKey = rs.getString("PK_NAME");
                    entry.getValue().setPrimaryKeyType(rs.getMetaData().getColumnType(4));
                }
                rs.close();
                rs = conn.getMetaData().getImportedKeys(null, null, entry.getKey());
                while (rs.next()) {
                    ForeignKey key = new ForeignKey(tables.get(rs.getString("FKTABLE_NAME")),
                            rs.getString("PKCOLUMN_NAME"));
                    entry.getValue().foreignKeys.put(key.localColumn, key);
                }
                rs.close();
                System.out.println(entry.getValue());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    @Override
    public void save() {
    }

    public class DatabaseKey extends DataKey {
        private final List<Runnable> actions = Lists.newArrayList();
        private String currentKey;
        private Table table;
        private String tableName;

        public DatabaseKey(String _table) {
            if (_table == null)
                throw new IllegalArgumentException("table cannot be null");
            if (_table.charAt(_table.length()) == 's')
                _table = _table.substring(0, _table.length() - 1);
            this.table = tables.get(_table);
            this.tableName = _table;
        }

        public DatabaseKey(Table table, String currentKey) {
            if (table == null || currentKey == null)
                throw new IllegalArgumentException("arguments cannot be null");
            this.table = table;
            this.currentKey = currentKey;
        }

        public DatabaseKey() {
        }

        @Override
        public void copy(String to) {
        }

        @Override
        public boolean getBoolean(String key) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public double getDouble(String key) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getInt(String key) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public List<DataKey> getIntegerSubKeys() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getLong(String key) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getRaw(String key) {
            return null;
        }

        @Override
        public DatabaseKey getRelative(String relative) {
            runActions();
            if (relative.isEmpty())
                return this;
            String[] split = relative.split("\\.");
            if (table == null) {
                String primary = null;
                if (tableName == null) {
                    tableName = split[0];
                    split = Arrays.copyOfRange(split, 1, split.length);
                    if (split.length > 1) {
                        primary = split[1];
                        split = Arrays.copyOfRange(split, 1, split.length);
                    }
                } else {
                    primary = split[0];
                    split = Arrays.copyOfRange(split, 1, split.length);
                }
                if (primary != null) {
                    table = createTable(tableName, INTEGER.matcher(primary).matches() ? Types.INTEGER : Types.VARCHAR);
                    currentKey = primary;
                }
            }
            String rel = split[0];
            // if (i + 1 == split.length && table.columns.contains(split[i])) {
            // continue;
            // }
            if (!tables.containsKey(rel)) {
                createTable(rel, Types.INTEGER);
            }
            Table foreign = tables.get(rel);
            if (!table.foreignKeys.containsKey("fk_" + foreign.name)) {
                createForeignKey(table, foreign);
            }
            ForeignKey mapping = table.foreignKeys.get("fk_" + foreign.name);

            Connection conn = getConnection();
            try {
                PreparedStatement stmt = conn.prepareStatement("SELECT `" + mapping.foreignTable.primaryKey
                        + "` FROM `" + mapping.foreignTable.name + "` INNER JOIN `" + table.name + "` ON `"
                        + mapping.foreignTable.primaryKey + "`=`" + mapping.localColumn + "`");
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    actions.add(new CreateRelation(this, mapping));
                }
                return new DatabaseKey(mapping.foreignTable, rs.getString(mapping.foreignTable.primaryKey));
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                DbUtils.closeQuietly(conn);
            }
            return null;
        }

        @Override
        public String getString(String key) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Iterable<DataKey> getSubKeys() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean keyExists(String key) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String name() {
            return currentKey != null ? currentKey : table.name;
        }

        @Override
        public void removeKey(String key) {
            Connection conn = getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement("DELETE FROM `" + table.name + "` WHERE `" + table.primaryKey + "` = `"
                        + key + "`");
                stmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DbUtils.closeQuietly(conn, stmt, null);
            }
        }

        @Override
        public void setBoolean(String key, boolean value) {
            setPrimitive(key, value);
        }

        @Override
        public void setDouble(String key, double value) {
            setPrimitive(key, value);
        }

        @Override
        public void setInt(String key, int value) {
            setPrimitive(key, value);
        }

        @Override
        public void setLong(String key, long value) {
            setPrimitive(key, value);
        }

        private void setPrimitive(String key, Object value) {
            runActions();
            String[] parts = Iterables.toArray(Splitter.on('.').split(key), String.class);
            String column = key, primaryReferenceKey = this.currentKey;
            Table from = this.table;
            if (parts.length > 1) {
                DatabaseKey traverse = new DatabaseKey(this.table, this.currentKey);
                for (int i = 0; i < parts.length - 1; ++i) {
                    traverse = traverse.getRelative(parts[i]);
                }
                traverse.runActions();
                from = traverse.table;
                primaryReferenceKey = traverse.currentKey;
                column = parts[parts.length - 1];
            }

            Connection conn = getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement("UPDATE `" + from.name + "` SET `" + column + "`=" + value + " WHERE `"
                        + from.primaryKey + "` =" + primaryReferenceKey);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                DbUtils.closeQuietly(conn, stmt, null);
            }
        }

        private void runActions() {
            for (Runnable run : actions) {
                run.run();
            }
            actions.clear();
        }

        @Override
        public void setRaw(String key, Object value) {
            setPrimitive(key, value);
        }

        @Override
        public void setString(String key, String value) {
            setPrimitive(key, value);
        }
    }

    public enum DatabaseType {
        H2("org.h2.Driver"),
        MYSQL("com.mysql.jdbc.Driver"),
        POSTGRE("org.postgresql.Driver"),
        SQLITE("org.sqlite.JDBC");
        private final String driver;
        private boolean loaded = false;

        DatabaseType(String driver) {
            this.driver = driver;
        }

        public boolean load() {
            if (loaded)
                return true;
            if (DbUtils.loadDriver(driver))
                loaded = true;
            return loaded;
        }

        public static DatabaseType match(String driver) {
            for (DatabaseType type : DatabaseType.values()) {
                if (type.name().toLowerCase().contains(driver)) {
                    return type;
                }
            }
            return null;
        }
    }

    private static class ForeignKey {
        final Table foreignTable;
        final String localColumn;

        ForeignKey(Table foreign, String from) {
            this.foreignTable = foreign;
            this.localColumn = from;
        }
    }

    private static class Table {
        final List<String> columns = Lists.newArrayList();
        final Map<String, ForeignKey> foreignKeys = Maps.newHashMap();
        String name;
        String primaryKey;
        int primaryKeyType;

        public Table setName(String tableName) {
            this.name = tableName;
            return this;
        }

        public Table setPrimaryKey(String pk) {
            this.primaryKey = pk;
            return this;
        }

        public Table setPrimaryKeyType(int type) {
            primaryKeyType = type;
            return this;
        }

        @Override
        public String toString() {
            return "Table [primaryKey=" + primaryKey + ", foreignKeys=" + foreignKeys + ", columns=" + columns + "]";
        }
    }

    private class CreateRelation implements Runnable {
        private final Table from;
        private final String primary;
        private final ForeignKey mapping;

        CreateRelation(DatabaseKey update, ForeignKey mapping) {
            this.from = update.table;
            this.primary = update.currentKey;
            this.mapping = mapping;
        }

        @Override
        public void run() {
            Connection conn = getConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn.prepareStatement("INSERT INTO ? () VALUES ()", Statement.RETURN_GENERATED_KEYS);
                stmt.executeUpdate();
                rs = stmt.getGeneratedKeys();
                rs.next();
                String generated = rs.getString(mapping.foreignTable.primaryKey);
                DbUtils.closeQuietly(null, stmt, rs);
                stmt = conn.prepareStatement("UPDATE `" + from.name + "` SET `" + mapping.localColumn + "`="
                        + generated + " WHERE `" + from.primaryKey + "`=`" + primary + "`");
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                DbUtils.closeQuietly(conn, stmt, rs);
            }
        }
    }

    private static final Pattern INTEGER = Pattern.compile("([\\+-]?\\d+)([eE][\\+-]?\\d+)?");
}