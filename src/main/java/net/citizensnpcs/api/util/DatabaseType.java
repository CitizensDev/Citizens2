package net.citizensnpcs.api.util;

public enum DatabaseType {
    H2("org.h2.Driver"),
    MYSQL("com.mysql.jdbc.Driver"),
    POSTGRE("org.postgresql.Driver"),
    SQLITE("org.sqlite.JDBC") {
        @Override
        public String getSpecialSyntaxFor(QueryType type) {
            if (type == QueryType.ADD_COLUMN)
                return "ADD COLUMN";
            return super.getSpecialSyntaxFor(type);
        }
    };
    private final String driver;
    private boolean loaded = false;

    DatabaseType(String driver) {
        this.driver = driver;
    }

    public boolean load() {
        if (loaded)
            return true;
        if (DatabaseStorage.loadDriver(DatabaseStorage.class.getClassLoader(), driver))
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

    public String getSpecialSyntaxFor(QueryType type) {
        return type.getDefaultSyntax();
    }
}