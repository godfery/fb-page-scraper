package cmdline;

import common.*;
import db.DbManager;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FbCollector
{
    public static long pageUpdateCount = 0;

    public static void main(String[] args) throws Exception
    {
        //FbCollector collector = new FbCollector();
    }

    public void collect()
    {
        if(isConfigValid())
        {
            if(Config.collectOnce)
            {

            }
            else
            {
                for(String page: Config.pages)
                {
                    PageCollector pageCollector = new PageCollector(page);
                    pageCollector.collect();
                }
                long curTime = System.currentTimeMillis();
                long sinceTime = Util.toMillis(Config.since, TimeZone.getTimeZone("UTC"));
                for(String page: Config.pages)
                {
                    PostsCollector postsCollector = new PostsCollector(new Page(page));
                    postsCollector.collect();
                }
            }
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
        return true;
    }
}
