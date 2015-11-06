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

    public static final long hourInMillis = 3600000;

    public static int scrapeCount = 0;

    public static long postsCount = 0;

    public static long commentsCount = 0;

    public static boolean collectStats;

    public static void main(String[] args) throws Exception
    {
        FbCollector collector = new FbCollector();
        collector.collect();
    }

    public void collect() throws InterruptedException
    {
        initTempSince();

        if(loopIndex == 0)
        {
            System.out.println(Util.getDbDateTimeEst() + " Started fetching data");
        }

        collectStats = loopIndex % 2 == 0;

        for(String page: Config.pages)
        {
            PageCollector pageCollector = new PageCollector(page);
            pageCollector.collect();
        }

        for(String page: Config.pages)
        {
            PostsCollector postsCollector = new PostsCollector(new Page(page));
            postsCollector.collect();
        }

        /* sleep after collecting stats */
        if(collectStats)
        {
            Util.sleep(300);
        }

        loopIndex++;

        if(tempSince == Util.toMillis(Config.since))
        {
            scrapeCount++;
            if(scrapeCount == 1)
            {
                System.out.println(Util.getDbDateTimeEst() + " Completed fetching all historic data from " + Config.since + " until " + Config.until);
                System.out.println("Continuing to fetch current and future data");
                Util.sleep(300);
            }
        }

        Config.init();
        if(!Config.collectOnce)
        {
            collect();
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
            tempSince = configUntil - hourInMillis;
        }
        else
        {
            tempSince = tempSince - hourInMillis;
        }
        if(tempSince < configSince)
        {
            tempSince = configSince;
        }
    }
}
