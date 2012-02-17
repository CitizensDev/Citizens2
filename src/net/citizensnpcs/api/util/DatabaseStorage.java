package net.citizensnpcs.api.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.google.common.collect.Lists;

public class DatabaseStorage implements Storage {
    private Connection conn;
    private final List<String> tables = Lists.newArrayList();
    private final String url, username, password;
    private final DatabaseType type;

    public DatabaseStorage(String driver, String url, String username, String password) throws SQLException {
        url = "jdbc:" + url;
        this.url = url;
        this.username = username;
        this.password = password;
        this.type = DatabaseType.match(driver);
        type.load();
        conn = DriverManager.getConnection(url, username, password);
        ResultSet table = conn.getMetaData().getTables(null, null, null, new String[] { "TABLE" });
        while (table.next()) {
            tables.add(table.getString(3));
        }
        table.close();
    }

    private void ensureConnection() throws SQLException {
        if (type == DatabaseType.SQLITE)
            return;

        if (!conn.isValid(0)) {
            conn = (username.isEmpty() && password.isEmpty()) ? DriverManager.getConnection(url) : DriverManager
                    .getConnection(url, username, password);
        }
    }

    @Override
    public DataKey getKey(String root) {
        for (String split : root.split("\\.")) {

        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void load() {
        // TODO Auto-generated method stub

    }

    @Override
    public void save() {
        // TODO Auto-generated method stub

    }

    public class DatabaseKey extends DataKey {

        @Override
        public void copy(String to) {
            // TODO Auto-generated method stub

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
            // TODO Auto-generated method stub
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
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void removeKey(String key) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setBoolean(String key, boolean value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setDouble(String key, double value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setInt(String key, int value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setLong(String key, long value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setRaw(String path, Object value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setString(String key, String value) {
            // TODO Auto-generated method stub

        }
    }

    public enum DatabaseType {
        MYSQL("com.mysql.jdbc.Driver"),
        H2("org.h2.Driver"),
        POSTGRE("org.postgresql.Driver"),
        SQLITE("org.sqlite.JDBC");
        private String driver;
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