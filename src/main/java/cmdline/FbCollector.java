package cmdline;

import common.Config;
import db.DbManager;

import java.sql.Connection;

public class FbCollector
{
    public static void main(String[] args) throws Exception
    {
        FbCollector collector = new FbCollector();
    }

    public void collect()
    {
        if(isConfigValid())
        {

        }
    }

    public boolean isConfigValid()
    {
        if(null == Config.accessToken || Config.accessToken.isEmpty())
        {
            System.err.println("accessToken missing");
            return false;
        }
        if(!DbManager.isParamsValid())
        {
            System.err.println("invalid database parameters");
            return false;
        }
        return true;
    }
}
