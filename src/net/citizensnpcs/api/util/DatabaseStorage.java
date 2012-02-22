package net.citizensnpcs.api.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        DataKey table = new DatabaseKey(split[0].isEmpty() ? null : split[0]);
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
                    ForeignKey key = new ForeignKey(tables.get(rs.getString("FKTABLE_NAME")), rs
                            .getString("PKCOLUMN_NAME"));
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

    private Table createTable(String name, int type) {
        String pk = name + "_id";
        String sql = "CREATE TABLE IF NOT EXISTS `" + name + "` ( `" + pk + "` ";
        switch (type) {
        case Types.INTEGER:
            sql += "int NOT NULL";
            break;
        case Types.VARCHAR:
            sql += "varchar(255) NOT NULL";
            break;
        default:
            throw new IllegalArgumentException("type not supported");
        }
        executeSQL(sql + " primary key (`" + pk + "`))");
        Table table = new Table().setName(name).setPrimaryKey(pk).setPrimaryKeyType(type);
        tables.put(name, table);
        return table;
    }

    private void createForeignKey(Table from, Table to) {
        String fk = "fk_" + to.name;
        String sql = "ALTER TABLE `" + from.name + "` ADD FOREIGN KEY (`" + fk + "`) REFERENCES " + to.name + "(`"
                + to.name + "_id`)";
        executeSQL(sql);
        from.foreignKeys.put(fk, new ForeignKey(to, fk));
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

    private static final Pattern INTEGER = Pattern.compile("([\\+-]?\\d+)([eE][\\+-]?\\d+)?");

    public class DatabaseKey extends DataKey {
        private Table table;
        private String tableName;
        private String currentKey;

        public DatabaseKey(String string) {
            if (string.charAt(string.length()) == 's')
                string = string.substring(0, string.length() - 1);
            this.table = tables.get(string);
            this.tableName = string;
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
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DataKey getRelative(String relative) {
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
            Connection conn = getConnection();
            for (int i = 0; i < split.length; ++i) {
                if (i + 1 == split.length && table.columns.contains(split[i])) {
                    continue;
                }
                if (!tables.containsKey(split[i])) {
                    createTable(split[i], Types.INTEGER);
                }
                Table foreign = tables.get(split[i]);
                if (!table.foreignKeys.containsKey("fk_" + foreign.name)) {
                    createForeignKey(table, foreign);
                }
                ForeignKey key = table.foreignKeys.get("fk_" + foreign.name);
                String name = key.foreignTable.name;
                try {
                    PreparedStatement stmt = conn.prepareStatement("SELECT `" + key.foreignTable.primaryKey
                            + "` FROM `" + name + "` INNER JOIN `" + table.name + "` ON `"
                            + key.foreignTable.primaryKey + "`=`" + key.localColumn + "`");
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) {
                        System.out.println("NO MATCHING RELATION");
                        // TODO: need to create a memo of some sort to create
                        // these relationships on setX, but fake it so that
                        // we're on the next table.
                        continue;
                    }
                    currentKey = rs.getString(key.foreignTable.primaryKey);
                    table = key.foreignTable;
                    System.out.println("switched to new table " + table.name);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            DbUtils.closeQuietly(conn);
            return this;
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
            return this.currentKey != null ? currentKey : table.name;
        }

        @Override
        public void removeKey(String key) {
            Connection conn = getConnection();
            if (conn == null)
                return;
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

        private void setPrimitive(String key, Object value) {
            try {
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement("UPDATE `" + table.name + "` SET `" + key + "`=" + value
                        + " WHERE `" + table.primaryKey + "` =" + this.currentKey);
                stmt.execute();
                DbUtils.closeQuietly(conn, stmt, null);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void setInt(String key, int value) {
            setPrimitive(key, value);
        }

        @Override
        public void setLong(String key, long value) {
            setPrimitive(key, value);
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

    private static class ForeignKey {
        final Table foreignTable;
        final String localColumn;

        ForeignKey(Table foreign, String from) {
            this.foreignTable = foreign;
            this.localColumn = from;
        }
    }

    private static class Table {
        String name;
        String primaryKey;
        int primaryKeyType;
        final Map<String, ForeignKey> foreignKeys = Maps.newHashMap();
        final List<String> columns = Lists.newArrayList();

        @Override
        public String toString() {
            return "Table [primaryKey=" + primaryKey + ", foreignKeys=" + foreignKeys + ", columns=" + columns + "]";
        }

        public Table setPrimaryKeyType(int type) {
            primaryKeyType = type;
            return this;
        }

        public Table setPrimaryKey(String pk) {
            this.primaryKey = pk;
            return this;
        }

        public Table setName(String tableName) {
            this.name = tableName;
            return this;
        }
    }

    public enum DatabaseType {
        MYSQL("com.mysql.jdbc.Driver"),
        H2("org.h2.Driver"),
        POSTGRE("org.postgresql.Driver"),
        SQLITE("org.sqlite.JDBC");
        private final String driver;
        private boolean loaded = false;

        DatabaseType(String driver) {
            this.driver = driver;
        }

        public static DatabaseType match(String driver) {
            for (DatabaseType type : DatabaseType.values()) {
                if (type.name().toLowerCase().contains(driver)) {
                    return type;
                }
            }
            return null;
        }

        public boolean load() {
            if (loaded)
                return true;
            if (DbUtils.loadDriver(driver))
                loaded = true;
            return loaded;
        }
    }
}