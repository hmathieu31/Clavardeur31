package com.insa.projet4a;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BDDManager {

    private String dbName = "test";

    public BDDManager(String path) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.dbName = path;
        } catch (ClassNotFoundException e) {
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

    public void initHistory() throws SQLException {

        String query = "CREATE TABLE IF NOT EXISTS history ("
                + " 'id' INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " 'user' TEXT NOT NULL,"
                + " 'from' BOOLEAN,"
                + " 'content' TEXT NOT NULL,"
                + " 'date' TEXT NOT NULL);";

        try (Connection conn = this.connect()) {
            Statement statement = conn.createStatement();
            statement.execute(query);
        } catch (SQLException e) {
            System.err.println("Error at initHistory");
        }
        ;
    }

    public void showHistory(String user) throws SQLException{
        String query = "SELECT * FROM history "
                        + "WHERE 'user' = " + user + ";";

        try (Connection conn = this.connect()){
            Statement statement = conn.createStatement();
            ResultSet result    = statement.executeQuery(query);
                
            // loop through the result set
            while (result.next()) {
                System.out.println(result.getBoolean("from") +  "\t" + 
                                result.getString("content") + "\t" +
                                result.getString("date"));
            }
        }
        catch(SQLException e) {
                System.err.println("Error at showHistory");
        };
    }

    public void insertHistory(String user, Boolean from, String content, String date) throws SQLException {
        String query = "INSERT INTO history('user','from','content','date') VALUES(?,?,?,?)";

        try (Connection conn = this.connect()) {
            PreparedStatement pStatement = conn.prepareStatement(query);

            pStatement.setString(1, user);
            pStatement.setBoolean(2, from);
            pStatement.setString(3, content);
            pStatement.setString(4, date);

            pStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error at insertHistory");
        };
    }
}
