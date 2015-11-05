package common;

import org.json.simple.JSONObject;


public class PageCollector
{
    private String page;

    public PageCollector(String page)
    {
        this.page = page;
    }

    public void collect()
    {
        String url = Config.baseUrl + "/" + page +
                "?fields=id,username,name,likes,talking_about_count,checkins,website,link,category,affiliation,about" +
                "&access_token=" + Config.accessToken;
        JSONObject pageJson = Util.getJson(url);
        if(null != pageJson)
        {
            Page page = new Page(pageJson);
            if(Config.collectJson)
            {
                page.writeJson();
            }
            page.updateDb();
        }
        else
        {
            System.err.println("cannot read data for facebook page: " + page);
        }
    }
}
