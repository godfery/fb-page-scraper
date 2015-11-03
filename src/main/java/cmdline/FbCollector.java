package cmdline;

import common.*;
import db.DbManager;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FbCollector
{
    public static long loopIndex = 0;

    public static long tempSince = -1;

    public static final long dayInMillis = 86400000;

    public static void main(String[] args) throws Exception
    {
        FbCollector collector = new FbCollector();
        collector.collect();
    }

    public void collect() throws InterruptedException
    {
        if(isConfigValid())
        {
            initTempSince();

            for(String page: Config.pages)
            {
                PageCollector pageCollector = new PageCollector(page);
                pageCollector.collect();

                PostsCollector postsCollector = new PostsCollector(new Page(page));
                postsCollector.collect();
            }

            loopIndex++;

            if(!Config.collectOnce)
            {
                Util.sleep(Config.waitTime);
                Config.init();
                collect();
            }
        }
    }

    public void initTempSince()
    {
        if(null == Config.until || Config.until.isEmpty() || !Config.until.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
        {
            Config.until = Util.getCurDateTimeUtc();
        }
        long configSince = Util.toMillis(Config.since);
        long configUntil = Util.toMillis(Config.until);
        if(tempSince == -1 || tempSince == configSince)
        {
            tempSince = configUntil - dayInMillis;
        }
        else
        {
            tempSince = tempSince - dayInMillis;
        }
        if(tempSince < configSince)
        {
            tempSince = configSince;
        }
    }

    public boolean isConfigValid()
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
