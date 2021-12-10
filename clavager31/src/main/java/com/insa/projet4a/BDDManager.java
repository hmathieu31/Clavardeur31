package com.insa.projet4a;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BDDManager {

    private String dbName;
    private Connection conn;

    public BDDManager(String path) {
            try {
                Class.forName("org.sqlite.JDBC");
                this.dbName = path;
                this.conn   = connect();
            } catch(ClassNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }

    private Connection connect() {
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

    public void initTable() throws SQLException{
        
        String query = "CREATE TABLE IF NOT EXISTS history ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "user TEXT NOT NULL"
                        + "from BOOLEAN,"
                        + "content TEXT NOT NULL,"
                        + "date TEXT NOT NULL);";
                        
		Statement statement = this.conn.createStatement();
		statement.execute(query);

        
        
    }

}
