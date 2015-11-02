package common;

import cmdline.FbCollector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class PostsCollector
{
    private Page page;

    private List<String> postIds = new ArrayList<String>();

    public PostsCollector(Page page)
    {
        this.page = page;
    }

    public void collect()
    {
        String tempSince = Config.since;
        String tempUntil = Config.until;

        if(!Config.collectOnce)
        {
            long curTime = System.currentTimeMillis();
            long configSince = Util.toMillis(Config.since);
            long configUntil = Util.toMillis(Config.until);
            if(FbCollector.postStats)
            {
                if(configUntil > (curTime - (2 * FbCollector.dayInMillis)))
                {
                    long statsSince = curTime - (2 * FbCollector.dayInMillis);
                    if(statsSince < configSince)
                    {
                        statsSince = configSince;
                    }
                    tempSince = Util.getDateTimeUtc(statsSince);
                }
            }
            else
            {
                if((configUntil - configSince) > FbCollector.dayInMillis)
                {
                    tempSince = Util.getDateTimeUtc(FbCollector.tempSince);
                    tempUntil = Util.getDateTimeUtc(FbCollector.tempSince + FbCollector.dayInMillis);
                }
            }
        }

        collect(getCollectUrl(tempSince, tempUntil));

        if(Config.collectOnce)
        {
            if(Config.collectComments)
            {
                for(String postId: postIds)
                {
                    CommentsCollector commentsCollector = new CommentsCollector(page.getUsername(), postId);
                    commentsCollector.collect();
                    try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
        }
        else
        {
            if(!FbCollector.postStats)
            {
                for(String postId: postIds)
                {
                    CommentsCollector commentsCollector = new CommentsCollector(page.getUsername(), postId);
                    commentsCollector.collect();
                }
            }
        }
    }

    private String getCollectUrl(String since, String until)
    {
        String url = Config.baseUrl + ("/") + (page.getUsername()) + "/posts";
        url += "?access_token=" + Config.accessToken;
        url += "&include_hidden=" + true;
        url += "&since=" + since;
        url += "&until=" + until;
        url += "&fields=id,message,created_time,shares,likes.limit(1).summary(true),comments.limit(1).summary(true),updated_time";
        //System.out.println(url);
        return url;
    }

    private void collect(String url)
    {
        JSONObject posts = Util.getJson(url);
        if(null != posts)
        {
            JSONArray postsData = (JSONArray) posts.get("data");
            Iterator itr = postsData.iterator();
            while (itr.hasNext())
            {
                JSONObject postJson = (JSONObject) itr.next();
                Post post = new Post(page, postJson);
                if(Config.collectJson)
                {
                    post.writeJson();
                }
                post.updateDb();
                postIds.add(post.getId());
            }
            JSONObject paging = (JSONObject) posts.get("paging");
            if(null != paging && null != paging.get("next"))
            {
                collect(paging.get("next").toString());
            }
        }
        else
        {
            System.err.println("reading posts failed for url: " + url);
        }
    }
}
