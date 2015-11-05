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
        return null != getFieldValue(table, key, key, value);
    }

    public static String getFieldValue(String table, String field, String keyName, String keyValue)
    {
        String fieldValue = null;
        Connection connection = DbManager.getConnection();
        String query = "SELECT " + field + " FROM " + table + " WHERE " + keyName + "=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            statement = connection.prepareStatement(query);
            statement.setString(1, keyValue);
            resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                fieldValue = resultSet.getString(1);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(null != resultSet) try { resultSet.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != statement) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return fieldValue;
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
            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return valid;
    }
}
