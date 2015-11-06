package common;

import db.DbManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Config
{
    public static final String baseUrl = "https://graph.facebook.com/v2.5";
    public static String accessToken;
    public static String jsonDir;
    public static boolean collectJson;
    public static boolean collectOnce;
    public static List<String> pages;
    public static String since;
    public static String until;
    public static String dbUrl;
    public static String dbUser;
    public static String dbPass;
    public static boolean collectComments;
    public static boolean crawlHistory;
    //public static int waitTime;
    static
    {
        init();
    }

    public static void init()
    {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try
        {
            if(new File("config.properties").exists())
            {
                inputStream = new FileInputStream("config.properties");
            }

            if(null == inputStream)
            {
                inputStream = Config.class.getClassLoader().getResourceAsStream("config.properties");
            }
            properties.load(inputStream);
            accessToken = properties.getProperty("accessToken");
            jsonDir = properties.getProperty("jsonDir");
            collectJson = properties.getProperty("collectJson").toLowerCase().equals("true");
            collectOnce = properties.getProperty("collectOnce").toLowerCase().equals("true");
            pages = Arrays.asList(properties.getProperty("pages").split("\\s*,\\s*"));
            since = properties.getProperty("since");
            until = properties.getProperty("until");
            dbUrl = properties.getProperty("dbUrl");
            dbUser = properties.getProperty("dbUser");
            dbPass = properties.getProperty("dbPass");
            collectComments = properties.getProperty("collectComments").toLowerCase().equals("true");
            crawlHistory = properties.getProperty("crawlHistory").toLowerCase().equals("true");
            //String tempWaitTime = properties.getProperty("waitTime");
            //waitTime = (null != tempWaitTime) && tempWaitTime.matches("\\d+") ? Integer.parseInt(tempWaitTime) : 60;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(null != inputStream)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if(!isConfigValid())
        {
            System.exit(0);
        }
    }

    public static boolean isConfigValid()
    {
        if(null == Config.accessToken || Config.accessToken.isEmpty())
        {
            System.err.println("accessToken missing");
            return false;
        }
        if(null == Config.dbUrl || null == Config.dbUser || null == Config.dbPass)
        {
            System.err.println("database connection parameters are required");
        }
        if(!DbManager.isParamsValid())
        {
            System.err.println("invalid database parameters");
            return false;
        }
        if(null == Config.since || Config.since.isEmpty() || !Config.since.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
        {
            System.err.println("invalid start date (since)");
            return false;
        }
        if(Config.collectJson)
        {
            if(null == Config.jsonDir || Config.jsonDir.isEmpty())
            {
                System.err.println("json directory is required if collectJson=true");
                return false;
            }
            File jsonDir = new File(Config.jsonDir);
            if(!jsonDir.exists() || !jsonDir.isDirectory())
            {
                System.err.println("invalid json directory");
                return false;
            }
        }
        return true;
    }
}
