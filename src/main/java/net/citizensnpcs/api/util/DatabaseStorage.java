package net.citizensnpcs.api.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;

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
    private static final Traversed INVALID_TRAVERSAL = new Traversed(null, null, null);
    private final Map<String, Table> tables = Maps.newHashMap();
    private final Map<String, Traversed> traverseCache = Maps.newHashMap();
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
        Connection conn = getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("ALTER TABLE `" + from.name + "` ADD FOREIGN KEY (`" + fk + "`) REFERENCES `"
                    + to.name + "` (`" + to.name + "_id" + "`)");
            stmt.execute();
            from.foreignKeys.put(fk, new ForeignKey(to, fk));
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn, stmt, null);
        }
    }

    private Table createTable(String name, int type) {
        String pk = name + "_id";
        String pkType = "";
        switch (type) {
        case Types.INTEGER:
            pkType = "INTEGER NOT NULL";
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
            stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + name + "` (`" + pk + "` " + pkType
                    + " PRIMARY KEY (`" + pk + "`))");
            stmt.execute();
            created = new Table().setName(name).setPrimaryKey(pk).setPrimaryKeyType(type);
            tables.put(name, created);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn, stmt, null);
        }
        return created;
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

    private static class Traversed {
        private final Table found;
        private final String key;
        private final String column;

        Traversed(Table found, String pk, String column) {
            this.found = found;
            this.key = pk;
            this.column = column;
        }
    }

    public class DatabaseKey extends DataKey {
        private final String current;

        private DatabaseKey(String root) {
            current = root;
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
            if (relative == null || relative.isEmpty())
                return this;
            return new DatabaseKey(createRelativeKey(relative));
        }

        @Override
        public String getString(String key) {
            Traversed t = traverse(createRelativeKey(key), false);
            if (t == INVALID_TRAVERSAL)
                return "";
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
            return traverse(createRelativeKey(key), false) != INVALID_TRAVERSAL;
        }

        @Override
        public String name() {
            Traversed t = traverse(current, true);
            return t.key != null ? t.key : t.found.name;
        }

        private Traversed traverse(String path, boolean createRelations) {
            Traversed prev = traverseCache.get(path);
            if (prev != null)
                return prev;
            String[] parts = Iterables.toArray(Splitter.on('.').split(path), String.class);
            if (parts.length < 2)
                return INVALID_TRAVERSAL; // not enough information given.
            Table table = null;
            String pk = null;
            for (int i = 0; i < parts.length - 1; ++i) {
                String part = parts[i];
                if (table == null && !tables.containsKey(part)) {
                    if (!createRelations || i + 1 >= parts.length)
                        return INVALID_TRAVERSAL;
                    String primary = parts[++i];
                    int type = INTEGER.matcher(primary).matches() ? Types.INTEGER : Types.VARCHAR;
                    table = createTable(part, type);
                    pk = table.primaryKey;
                    continue;
                }
            }
            Traversed t = new Traversed(table, pk, parts[parts.length - 1]);
            traverseCache.put(path, t);
            return t;
        }

        @Override
        public void removeKey(String key) {
            Traversed t = traverse(createRelativeKey(key), false);
            if (t == INVALID_TRAVERSAL)
                return;
        }

        @Override
        public void setBoolean(String key, boolean value) {
            setPrimitive(key, value);
        }

        private void setPrimitive(String key, Object value) {
            Traversed t = traverse(createRelativeKey(key), true);
            Connection conn = getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement("UPDATE `" + t.found.name + "` SET `" + t.column + "`= ? WHERE `"
                        + t.found.primaryKey + "` = ?");
                stmt.setString(1, value.toString());
                stmt.setString(2, t.key);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                DbUtils.closeQuietly(conn, stmt, null);
            }
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

        @Override
        public void setRaw(String key, Object value) {
            setPrimitive(key, value);
        }

        @Override
        public void setString(String key, String value) {
            setPrimitive(key, value);
        }
    }

    /* public class x extends DataKey {
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

         /*
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
                 // if (!rs.next()) {
                 // actions.add(new CreateRelation(this, mapping));
                 // }
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
                 stmt = conn.prepareStatement("DELETE FROM `" + table.name + "` WHERE `" + table.primaryKey + "` = ?");
                 stmt.setString(1, key);
                 stmt.executeUpdate();
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
         }

         @Override
         public void setRaw(String key, Object value) {
             setPrimitive(key, value);
         }

         @Override
         public void setString(String key, String value) {
             setPrimitive(key, value);
         }
     }*/

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

    /* private class CreateRelation implements Runnable {
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
                         + generated + " WHERE `" + from.primaryKey + "`= ?");
                 stmt.setString(1, primary);
                 stmt.executeUpdate();
             } catch (SQLException ex) {
                 ex.printStackTrace();
             } finally {
                 DbUtils.closeQuietly(conn, stmt, rs);
             }
         }
     }*/

    private static final Pattern INTEGER = Pattern.compile("([\\+-]?\\d+)([eE][\\+-]?\\d+)?");
}