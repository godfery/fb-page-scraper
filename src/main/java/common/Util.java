package common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Util
{
    public static DateFormat dbDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getDbDateTimeEst()
    {
        dbDateFormatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = new Date(System.currentTimeMillis());
        return dbDateFormatter.format(date);
    }

    public static String getDbDateTimeUtc()
    {
        dbDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(System.currentTimeMillis());
        return dbDateFormatter.format(date);
    }

    public static String getCurDateTimeDir()
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        return formatter.format(date);
    }

    public static long toMillis(String dateString, TimeZone timeZone)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(timeZone);
        long time = 0;
        try
        {
            time = formatter.parse(dateString).getTime();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return time;
    }

    public static String toDbDateTime(String utcDate)
    {
        return utcDate.replaceFirst("\\+[\\d]+","").replaceFirst("T", " ");
    }

    /**
     * Build path for writing json file
     * Create directories along the path under main json collection directory
     */
    public static String buildPath(String... directories)
    {
        String dir = Config.jsonDir;
        for(String temp: directories)
        {
            dir = dir + "/" + temp;
            if(!(new File(dir).exists()))
            {
                new File(dir).mkdir();
            }
        }
        return dir;
    }

    public static synchronized JSONObject getJson(String url)
    {
        JSONObject json = null;
        InputStream is = null;
        try
        {
            is = new URL(url).openStream();
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(new InputStreamReader(is, Charset.forName("UTF-8")));
        }
        catch (Exception e)
        {
            System.err.println("reading failed for url: " + url);
        }
        finally
        {
            try { if(null != is) is.close(); } catch (Exception e) { e.printStackTrace(); }
        }
        return json;
    }
}
