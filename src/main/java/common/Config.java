package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Config
{
    public static final String baseUrl = "https://graph.facebook.com/v2.0";
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
    public static boolean pageCrawl;
    public static boolean postCrawl;
    public static int waitTime;
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
            pageCrawl = properties.getProperty("pageCrawl").toLowerCase().equals("true");
            postCrawl = properties.getProperty("postCrawl").toLowerCase().equals("true");
            String tempWaitTime = properties.getProperty("waitTime");
            waitTime = (null != tempWaitTime) && tempWaitTime.matches("\\d+") ? Integer.parseInt(tempWaitTime) : 10;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(null != inputStream)
            {
                try{ inputStream.close(); }
                catch (IOException e) { e.printStackTrace(); }
            }
        }
    }
}
