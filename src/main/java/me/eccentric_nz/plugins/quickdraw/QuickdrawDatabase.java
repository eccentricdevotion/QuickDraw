package me.eccentric_nz.plugins.quickdraw;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QuickdrawDatabase {

    private static QuickdrawDatabase instance = new QuickdrawDatabase();
    public Connection connection = null;
    public Statement statement;
    private Quickdraw plugin;

    public static synchronized QuickdrawDatabase getInstance() {
        return instance;
    }

    public void setConnection(String path) throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTables() {
        try {
            statement = connection.createStatement();
            String queryQuickDraw = "CREATE TABLE IF NOT EXISTS quickdraw (qd_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, player TEXT, versus TEXT, time INTEGER)";
            statement.executeUpdate(queryQuickDraw);
            String queryInventories = "CREATE TABLE IF NOT EXISTS inventories (inv_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, player TEXT, inventory TEXT)";
            statement.executeUpdate(queryInventories);
            statement.close();
        } catch (SQLException e) {
            plugin.debug("Create table error: " + e);
        }
    }

    public ResultSet getRecords(String query) {
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(query);
            statement.close();
        } catch (SQLException e) {
            plugin.debug("Could not get ResultSet for: " + query + " Error: " + e);
        }
        return rs;
    }

    public void doUpdate(String query) {
        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            plugin.debug("Could not do update/insert for: " + query + " Error: " + e);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }
}
