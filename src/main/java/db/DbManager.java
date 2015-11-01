package db;

import common.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbManager
{
    public static Connection getConnection()
    {
        Connection connection = null;
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(Config.dbUrl, Config.dbUser, Config.dbPass);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return connection;
    }

    public static boolean entryExists(String table, String key, String value)
    {
        boolean exists = false;
        Connection connection = DbManager.getConnection();
        String query = "SELECT " + key + " FROM " + table + " WHERE " + key + "=?";
        try
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, value);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                exists = null != resultSet.getString(1);
            }
            resultSet.close();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return exists;
    }

    public static boolean isParamsValid()
    {
        boolean valid = false;
        Connection connection = getConnection();
        try
        {
            valid = connection.isValid(0);
        }
        catch (SQLException e)
        {
            System.err.println("database connection parameters are invalid");
        }
        finally
        {
            try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return valid;
    }
}
