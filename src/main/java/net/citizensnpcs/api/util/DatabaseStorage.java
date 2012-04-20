package net.citizensnpcs.api.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Implements a traversable tree-view database, which can be accessed with
 * simple keys, and is dynamic, meaning that any necessary tables or columns
 * must be created when needed.
 * 
 * Keys are formatted using a general node-subnode format x.x.x...x, and will be
 * treated by this object according to a set of rules.
 * <ul>
 * <li>1. The first node is treated as the name of the initial table.
 * 
 * <li>2. The second node is treated as the primary key of the given table name,
 * and will be fetched or created from the database as necessary.
 * 
 * <p>
 * A combination of the first two rules gives us the initial starting point to
 * traverse the key. We have a table and a valid starting point to begin at.
 * </p>
 * 
 * <li>3. The last node of the key is treated as the table field to fetch or set
 * the requested value from.
 * 
 * <li>4. Any other subnodes are traversed by creating foreign keys between the
 * current table and the next table.
 * </ul>
 * 
 * <p>
 * For example, the user sets a string at the key
 * <code>npcs.Bob.location.x</code>
 * </p>
 * <ul>
 * <li>The table <code>npcs</code> is created.
 * 
 * <li>The primary key field must also be created, and as <code>Bob</code> does
 * not match any other validation patterns, it is treated as a varchar, so a
 * primary key <code>npcs_id varchar(255)</code> is created.
 * 
 * <li>The table <code>location</code> is created and a foreign key
 * <code>fk_location</code> is inserted into <code>npcs</code> which references
 * <code>location_id</code> (integer). A row is created in location and the
 * fk_location field is updated with the generated id.
 * 
 * <li>The field <code>x varchar(255)</code> is created for
 * <code>location</code> and the value inserted.
 * </ul>
 */
public class DatabaseStorage implements Storage {
    private Connection conn;
    private final QueryRunner queryRunner = new QueryRunner();
    private final Map<String, Table> tables = Maps.newHashMap();
    private final Map<String, Traversed> traverseCache = Maps.newHashMap();
    private final String url, username, password;

    public DatabaseStorage(String driver, String url, String username, String password) throws SQLException {
        url = "jdbc:" + url;
        this.url = url;
        this.username = username;
        this.password = password;
        DatabaseType.match(driver).load();
    }

    private void createForeignKey(Table from, Table to) {
        String fk = "fk_" + to.name;
        if (from.foreignKeys.containsKey(fk)) {
            return;
        }
        Connection conn = getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("ALTER TABLE `" + from.name + "` ADD " + fk + " " + to.primaryKeyType);
            stmt.execute();
            closeQuietly(stmt);
            stmt = conn.prepareStatement("ALTER TABLE `" + from.name + "` ADD FOREIGN KEY (`" + fk + "`) REFERENCES `"
                    + to.name + "` (`" + to.name + "_id" + "`)");
            stmt.execute();
            from.addForeignKey(fk, new ForeignKey(fk));
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeQuietly(stmt);
        }
    }

    private Table createTable(String name, int type, boolean autoIncrement) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");
        Table t = tables.get(name);
        if (t != null)
            return t;
        String pk = name + "_id";
        String pkType = "";
        switch (type) {
        case Types.INTEGER:
            pkType = "INTEGER NOT NULL";
            if (autoIncrement)
                pkType += " AUTOINCREMENT";
            break;
        case Types.VARCHAR:
            pkType = "varchar(255) NOT NULL";
            break;
        default:
            throw new IllegalArgumentException("type not supported");
        }

        Connection conn = getConnection();
        PreparedStatement stmt = null;
        Table created = null;
        try {
            stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + name + "(`" + pk + "` " + pkType
                    + ", PRIMARY KEY (`" + pk + "`))");
            stmt.execute();
            created = new Table().setName(name).setPrimaryKey(pk).setPrimaryKeyType(pkType);
            tables.put(name, created);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return created;
    }

    private String ensureRelation(String pk, Table from, final Table to) {
        Connection conn = getConnection();
        try {
            String existing = queryRunner.query(conn, "SELECT `fk_" + to.name + "` FROM " + from.name + " WHERE "
                    + from.primaryKey + " = ?", new ResultSetHandler<String>() {
                @Override
                public String handle(ResultSet rs) throws SQLException {
                    return rs.getString("fk_" + to.name);
                }
            }, pk);
            if (existing == null) {
                String generated = to.generateRow();
                queryRunner.update(conn, "UPDATE `" + from.name + "` SET `fk_" + to.name + "=?", generated);
                return generated;
            } else {
                return existing;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Connection getConnection() {
        if (conn != null) {
            // Make a dummy query to check the connection is alive.
            try {
                conn.prepareStatement("SELECT 1;").execute();
            } catch (SQLException ex) {
                if ("08S01".equals(ex.getSQLState())) {
                    closeQuietly(conn);
                }
            }
        }
        try {
            if (conn == null || conn.isClosed()) {
                conn = (username.isEmpty() && password.isEmpty()) ? DriverManager.getConnection(url) : DriverManager
                        .getConnection(url, username, password);
                return conn;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        return conn;
    }

    @Override
    public DataKey getKey(String root) {
        return new DatabaseKey(root);
    }

    @Override
    public void load() {
        tables.clear();
        traverseCache.clear();
        Connection conn = getConnection();
        try {
            ResultSet rs = conn.getMetaData().getTables(null, null, null, new String[] { "TABLE" });
            while (rs.next()) {
                tables.put(rs.getString("TABLE_NAME"), new Table());
            }
            rs.close();
            for (Entry<String, Table> entry : tables.entrySet()) {
                Table table = entry.getValue();
                table.name = entry.getKey();
                rs = conn.getMetaData().getColumns(null, null, entry.getKey(), null);
                while (rs.next()) {
                    table.columns.add(rs.getString("COLUMN_NAME"));
                }
                rs.close();
                rs = conn.getMetaData().getPrimaryKeys(null, null, entry.getKey());
                while (rs.next()) {
                    table.primaryKey = rs.getString("COLUMN_NAME");
                    table.setPrimaryKeyType(rs.getMetaData().getColumnTypeName(4));
                }
                rs.close();
                rs = conn.getMetaData().getImportedKeys(null, null, entry.getKey());
                while (rs.next()) {
                    assert rs.getString("PKCOLUMN_NAME") != null;
                    ForeignKey key = new ForeignKey(rs.getString("PKCOLUMN_NAME"));
                    table.foreignKeys.put(key.localColumn, key);
                }
                rs.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void save() {
        commitAndCloseQuietly(conn);
    }

    @Override
    public String toString() {
        return "DatabaseStorage {url=" + url + ", username=" + username + ", password=" + password + "}";
    }

    public class DatabaseKey extends DataKey {
        private final String current;

        private DatabaseKey(String root) {
            current = root;
        }

        private DatabaseKey() {
            this("");
        }

        private String createRelativeKey(String from) {
            if (from.isEmpty())
                return current;
            if (from.charAt(0) == '.')
                return current.isEmpty() ? from.substring(1, from.length()) : current + from;
            return current.isEmpty() ? from : current + "." + from;
        }

        @Override
        public boolean getBoolean(String key) {
            final Traversed t = traverse(createRelativeKey(key), false);
            if (t == INVALID_TRAVERSAL)
                return false;
            Boolean value = getValue(t, new ResultSetHandler<Boolean>() {
                @Override
                public Boolean handle(ResultSet rs) throws SQLException {
                    return rs.getBoolean(t.column);
                }
            });
            return value == null ? false : value;
        }

        @Override
        public double getDouble(String key) {
            final Traversed t = traverse(createRelativeKey(key), false);
            if (t == INVALID_TRAVERSAL)
                return 0;
            Double value = getValue(t, new ResultSetHandler<Double>() {
                @Override
                public Double handle(ResultSet rs) throws SQLException {
                    return rs.getDouble(t.column);
                }
            });
            return value == null ? 0 : value;
        }

        @Override
        public int getInt(String key) {
            final Traversed t = traverse(createRelativeKey(key), false);
            if (t == INVALID_TRAVERSAL)
                return 0;
            Integer value = getValue(t, new ResultSetHandler<Integer>() {
                @Override
                public Integer handle(ResultSet rs) throws SQLException {
                    return rs.getInt(t.column);
                }
            });
            return value == null ? 0 : value;
        }

        @Override
        public long getLong(String key) {
            final Traversed t = traverse(createRelativeKey(key), false);
            if (t == INVALID_TRAVERSAL)
                return 0;
            Long value = getValue(t, new ResultSetHandler<Long>() {
                @Override
                public Long handle(ResultSet rs) throws SQLException {
                    return rs.getLong(t.column);
                }
            });
            return value == null ? 0 : value;
        }

        @Override
        public Object getRaw(String key) {
            final Traversed t = traverse(createRelativeKey(key), false);
            if (t == INVALID_TRAVERSAL)
                return null;
            Object value = getValue(t, new ResultSetHandler<Object>() {
                @Override
                public Object handle(ResultSet rs) throws SQLException {
                    return rs.getObject(t.column);
                }
            });
            return value;
        }

        @Override
        public DataKey getRelative(String relative) {
            if (relative == null || relative.isEmpty())
                return this;
            return new DatabaseKey(createRelativeKey(relative));
        }

        protected Traversed getRoot() {
            return null;
        }

        @Override
        public String getString(String key) {
            final Traversed t = traverse(createRelativeKey(key), false);
            if (t == INVALID_TRAVERSAL)
                return "";
            String value = getValue(t, new ResultSetHandler<String>() {
                @Override
                public String handle(ResultSet rs) throws SQLException {
                    return rs.getString(t.column);
                }
            });
            return value == null ? "" : value;
        }

        @Override
        public Iterable<DataKey> getSubKeys() {
            List<DataKey> keys = Lists.newArrayList();
            if (current.split("\\.").length == 1) {
                return getSingleKeys(keys);
            }
            // TODO: handle longer case
            return keys;
        }

        private Iterable<DataKey> getSingleKeys(List<DataKey> keys) {
            if (!tables.containsKey(current))
                return keys;
            Table table = tables.get(current);
            if (table.primaryKey == null)
                return keys;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                Connection conn = getConnection();
                stmt = conn.prepareStatement("SELECT `" + table.primaryKey + "` FROM `" + current + "`");
                rs = stmt.executeQuery();
                while (rs.next()) {
                    final Traversed found = new Traversed(table, rs.getString(table.primaryKey), table.primaryKey);
                    keys.add(new DatabaseKey() {
                        @Override
                        public Traversed getRoot() {
                            return found;
                        }
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeQuietly(stmt);
                closeQuietly(rs);
            }
            return keys;
        }

        private <T> T getValue(Traversed t, ResultSetHandler<T> resultSetHandler) {
            try {
                return queryRunner.query(getConnection(), "SELECT `" + t.column + "` FROM " + t.found.name + " WHERE `"
                        + t.found.primaryKey + "`=?", resultSetHandler, t.key);
            } catch (SQLException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        public boolean keyExists(String key) {
            return traverse(createRelativeKey(key), false) != INVALID_TRAVERSAL;
        }

        @Override
        public String name() {
            Traversed t = traverse(current, true);
            return t.key != null ? t.key : t.found.name;
        }

        @Override
        public void removeKey(String key) {
            Traversed t = traverse(createRelativeKey(key), false);
            if (t == INVALID_TRAVERSAL)
                return;
            Connection conn = getConnection();
            try {
                if (t.found.columns.contains(t.column)) {
                    queryRunner.update(conn, "UPDATE `" + t.found.name + "` SET `" + t.column + "`=? WHERE `"
                            + t.found.primaryKey + "`=?", null, t.key);
                } else {
                    queryRunner.update(conn, "DELETE FROM `" + t.found.name + "` WHERE `" + t.found.primaryKey + "=?",
                            t.key);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void setBoolean(String key, final boolean value) {
            setValue("SMALLINT", key, value);
        }

        @Override
        public void setDouble(String key, final double value) {
            setValue("DOUBLE", key, value);
        }

        @Override
        public void setInt(String key, final int value) {
            setValue("STRING", key, value);
        }

        @Override
        public void setLong(String key, final long value) {
            setValue("BIGINT", key, value);
        }

        @Override
        public void setRaw(String key, final Object value) {
            setValue("JAVA_OBJECT", key, value);
        }

        @Override
        public void setString(String key, final String value) {
            setValue("VARCHAR", key, value);
        }

        private void setValue(String type, String key, Object value) {
            Traversed t = traverse(createRelativeKey(key), true);
            if (t == INVALID_TRAVERSAL)
                throw new IllegalStateException("could not set " + value + " at " + key);
            Connection conn = getConnection();
            try {
                if (!t.found.columns.contains(t.column)) {
                    PreparedStatement stmt = conn.prepareStatement("ALTER TABLE `" + t.found.name + "` ADD `"
                            + t.column + "` " + type);
                    stmt.execute();
                    closeQuietly(stmt);
                    t.found.columns.add(t.column);
                }
                queryRunner.update(conn, "UPDATE `" + t.found.name + "` SET `" + t.column + "`= ? WHERE `"
                        + t.found.primaryKey + "` = ?", value, t.key);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private Traversed traverse(String path, boolean createRelations) {
            Traversed prev = traverseCache.get(path);
            if (prev != null)
                return prev;
            String[] parts = Iterables.toArray(Splitter.on('.').omitEmptyStrings().trimResults().split(path),
                    String.class);
            Traversed root = getRoot();
            if (root == null && parts.length < 2)
                return INVALID_TRAVERSAL; // not enough information given.
            Table table = root != null ? root.found : null;
            String pk = root != null ? root.key : null;
            for (int i = 0; i < parts.length - 1; ++i) {
                String part = parts[i];
                boolean contains = tables.containsKey(part);
                if (table == null) {
                    if (!createRelations || i + 1 >= parts.length)
                        return INVALID_TRAVERSAL;
                    pk = parts[++i];
                    int type = INTEGER.matcher(pk).matches() ? Types.INTEGER : Types.VARCHAR;
                    table = createTable(part, type, false);
                    if (table == null)
                        return INVALID_TRAVERSAL;
                    table.insert(pk);
                    continue;
                } else if (!contains) {
                    if (createTable(part, Types.INTEGER, true) == null)
                        return INVALID_TRAVERSAL;
                }
                Table next = tables.get(part);
                if (!table.foreignKeys.containsKey("fk_" + part)) {
                    if (!createRelations)
                        return INVALID_TRAVERSAL;
                    createForeignKey(table, next);
                }
                pk = ensureRelation(pk, table, next);
                if (pk == null)
                    return INVALID_TRAVERSAL;
                table = next;
            }
            String setColumn = parts.length == 0 ? null : parts[parts.length - 1];
            Traversed t = new Traversed(table, pk, setColumn);
            traverseCache.put(path, t);
            return t;
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
            if (loadDriver(DatabaseStorage.class.getClassLoader(), driver))
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
        final String localColumn;

        ForeignKey(String from) {
            this.localColumn = from;
        }
    }

    private class Table {
        final List<String> columns = Lists.newArrayList();
        final Map<String, ForeignKey> foreignKeys = Maps.newHashMap();
        String name;
        String primaryKey;
        String primaryKeyType;

        public void addForeignKey(String fk, ForeignKey foreignKey) {
            foreignKeys.put(fk, foreignKey);
            columns.add(fk);
        }

        public String generateRow() {
            String vals = Joiner.on(", ").join(columns);
            StringBuilder nullBuilder = new StringBuilder();
            for (int i = 0; i < columns.size(); ++i) {
                nullBuilder.append("NULL,");
            }
            String nulls = nullBuilder.substring(0, nullBuilder.length() - 2).toString();
            Connection conn = getConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn.prepareStatement("INSERT INTO `" + name + "` (" + vals + ") VALUES (" + nulls + ")",
                        Statement.RETURN_GENERATED_KEYS);
                stmt.executeQuery();
                rs = stmt.getGeneratedKeys();
                if (!rs.next())
                    return null;
                return rs.getString(primaryKey);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        public void insert(String primary) {
            Connection conn = getConnection();
            try {
                queryRunner.update(conn, "INSERT INTO `" + name + "` (`" + primaryKey + "`) VALUES (?)", primary);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public Table setName(String tableName) {
            name = tableName;
            return this;
        }

        public Table setPrimaryKey(String pk) {
            this.primaryKey = pk;
            return this;
        }

        public Table setPrimaryKeyType(String type) {
            primaryKeyType = type;
            return this;
        }

        @Override
        public String toString() {
            return "Table {name=" + name + ", primaryKey=" + primaryKey + ", foreignKeys=" + foreignKeys + ", columns="
                    + columns + "}";
        }
    }

    private static class Traversed {
        private final String column;
        private final Table found;
        private final String key;

        Traversed(Table found, String pk, String column) {
            this.found = found;
            this.key = pk;
            this.column = column;
        }
    }

    private static final Pattern INTEGER = Pattern.compile("([\\+-]?\\d+)([eE][\\+-]?\\d+)?");
    private static final Traversed INVALID_TRAVERSAL = new Traversed(null, null, null);

    // methods from Apache's DbUtils
    private static void closeQuietly(Connection conn) {
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
        }
    }

    private static void closeQuietly(Statement stmt) {
        try {
            if (stmt != null)
                stmt.close();
        } catch (SQLException e) {
        }
    }

    private static void closeQuietly(ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
        }
    }

    private static void commitAndCloseQuietly(Connection conn) {
        try {
            try {
                conn.commit();
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
        }
    }

    public static boolean loadDriver(ClassLoader classLoader, String driverClassName) {
        try {
            classLoader.loadClass(driverClassName).newInstance();
            return true;

        } catch (IllegalAccessException e) {
            // Constructor is private, OK for DriverManager contract
            return true;

        } catch (Exception e) {
            return false;

        }
    }
}