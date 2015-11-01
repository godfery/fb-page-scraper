package common;

import cmdline.FbCollector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class PostsCollector
{
    private Page page;

    public PostsCollector(Page page)
    {
        this.page = page;
    }

    public void collect()
    {
        String url = Config.baseUrl + ("/") + (page.getUsername()) + "/posts";
        url += "?access_token=" + Config.accessToken;
        url += "&include_hidden=" + true;
        url += "&since=" + Config.since;
        url += "&until=" + Config.until;
        url += "&fields=id,message,created_time,shares,likes.limit(1).summary(true),comments.limit(1).summary(true),updated_time";
        collect(url);
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
                post.writeJson();
                post.updateDb();
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
