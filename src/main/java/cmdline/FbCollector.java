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
    public static long sincePointer = 0;

    public static long untilPointer = System.currentTimeMillis();

    public static final long dayInMillis = 86400000;

    public static final long hourInMillis = 3600000;

    public static final long timeSlice = hourInMillis * (24/(Config.pages.size() > 24 ? 24 : Config.pages.size()));

    public static int scrapeCount = 0;

    public static boolean collectStats;

    public static void main(String[] args) throws Exception
    {
        FbCollector collector = new FbCollector();

        System.out.println(Util.getDbDateTimeEst() + " started fetching data");

        if(Config.collectOnce)
        {
            collector.collectOnce();
        }
        else
        {
            collector.collectStatsData();
        }
    }

    public void collectOnce()
    {
        if(null == Config.until || Config.until.isEmpty() || !Config.until.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
        {
            Config.until = Util.getCurDateTimeUtc();
        }

        for(String page: Config.pages)
        {
            PageCollector pageCollector = new PageCollector(page);
            pageCollector.collect();
        }

        for(String page: Config.pages)
        {
            PostsCollector postsCollector = new PostsCollector(new Page(page));
            postsCollector.collectOnce();
        }
    }

    public void collectStatsData()
    {
        collectStats = true;

        initUntilPointer();

        long configSince = Util.toMillis(Config.since);
        long statsSince = System.currentTimeMillis() - (2 * FbCollector.dayInMillis);
        if(FbCollector.untilPointer > statsSince)
        {
            statsSince = (configSince < statsSince) ? statsSince : configSince;
            String tempSince = Util.getDateTimeUtc(statsSince);
            String tempUntil = Util.getDateTimeUtc(FbCollector.untilPointer);
            System.out.println(Util.getDbDateTimeEst() + " fetching stats data from " + tempSince + " to " + tempUntil);
        }

        int tempPostsCount = 0;
        for(String page: Config.pages)
        {
            PageCollector pageCollector = new PageCollector(page);
            pageCollector.collect();

            PostsCollector postsCollector = new PostsCollector(new Page(page));
            postsCollector.collect();

            tempPostsCount += postsCollector.postIds.size();
        }
        System.out.println(Util.getDbDateTimeEst() + " fetched " + tempPostsCount + " posts");

        Util.sleep(Config.waitTime);

        collectHistoricData();
    }

    public void collectHistoricData()
    {
        collectStats = false;

        initUntilPointer();

        initSincePointer();

        String tempSince = Util.getDateTimeUtc(FbCollector.sincePointer);
        String tempUntil = Util.getDateTimeUtc(FbCollector.sincePointer + FbCollector.timeSlice);
        System.out.println(Util.getDbDateTimeEst() + " fetching historic data from " + tempSince + " to " + tempUntil);

        for(String page: Config.pages)
        {
            PageCollector pageCollector = new PageCollector(page);
            pageCollector.collect();
        }

        int tempPostsCount = 0;
        int tempCommentsCount = 0;
        for(String page: Config.pages)
        {
            PostsCollector postsCollector = new PostsCollector(new Page(page));
            postsCollector.collect();

            tempPostsCount += postsCollector.postIds.size();
            tempCommentsCount += postsCollector.commentsCount;
        }
        System.out.println(Util.getDbDateTimeEst() + " fetched " + tempPostsCount + " posts, " + tempCommentsCount + " comments");

        if(tempCommentsCount > 5000)
        {
            Util.sleep(Config.waitTime);
        }

        if(sincePointer == Util.toMillis(Config.since))
        {
            scrapeCount++;

            System.out.println(Util.getDbDateTimeEst() + " scraped " + scrapeCount + " time(s)");

            Util.sleep(Config.waitTime);

            Config.init();

            if(!Config.collectOnce)
            {
                collectStatsData();
            }
        }
        else
        {
            collectStatsData();
        }
    }

    private void initUntilPointer()
    {
        if(null == Config.until || Config.until.isEmpty() || !Config.until.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
        {
            untilPointer = System.currentTimeMillis();
        }
        else
        {
            untilPointer = Util.toMillis(Config.until);
        }
    }

    private void initSincePointer()
    {
        long configSince = Util.toMillis(Config.since);

        if(sincePointer == 0 || sincePointer == configSince)
        {
            sincePointer = untilPointer - timeSlice;
        }
        else
        {
            sincePointer = sincePointer - timeSlice;
        }
        if(sincePointer < configSince)
        {
            sincePointer = configSince;
        }
    }
}
