package com.paccothetaco.DiscordBot;

import com.paccothetaco.DiscordBot.Utils.SecretUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = SecretUtil.getDbUrl();
            String user = SecretUtil.getDbUser();
            String password = SecretUtil.getDbPass();
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }
}
