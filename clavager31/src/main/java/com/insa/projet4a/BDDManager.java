package com.insa.projet4a;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BDDManager {

    private String dbName;

    public BDDManager(String path) {
            try {
                Class.forName("org.sqlite.JDBC");
                this.dbName = path;
            } catch(ClassNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }

    public Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:clavager31/src/main/resources/com/insa/projet4a/sqlite/db/" + this.dbName + ".db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
