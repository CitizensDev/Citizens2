package net.citizensnpcs.api.util;

public enum QueryType {
    ADD_COLUMN("ADD");
    private final String defaultSyntax;

    QueryType(String defaultSyntax) {
        this.defaultSyntax = defaultSyntax;

    }

    public String getDefaultSyntax() {
        return this.defaultSyntax;
    }
}
