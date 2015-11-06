package common;

import cmdline.FbCollector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class PostsCollector
{
    private Page page;

    public List<String> postIds;

    public int commentsCount = 0;

    public PostsCollector(Page page)
    {
        this.page = page;
        postIds = new ArrayList<String>();
    }

    public void collectOnce()
    {
        /**
         * Collect posts from tempSince to tempUntil
         */
        String url = getCollectUrl(Config.since, Config.until);
        collect(url);

        /**
         * Collect comments for the above posts
         */
        if(Config.collectComments)
        {
            for(String postId: postIds)
            {
                CommentsCollector commentsCollector = new CommentsCollector(page.getUsername(), postId);
                commentsCollector.collect();
            }
        }
    }

    public void collect()
    {
        if(FbCollector.collectStats)
        {
            long configSince = Util.toMillis(Config.since);
            long statsSince = System.currentTimeMillis() - (2 * FbCollector.dayInMillis);
            if(FbCollector.untilPointer > statsSince)
            {
                statsSince = (configSince < statsSince) ? statsSince : configSince;
                /* collect posts and stats only */
                String url = getCollectUrl(Util.getDateTimeUtc(statsSince), Util.getDateTimeUtc(FbCollector.untilPointer));
                collect(url);
            }
        }
        else
        {
            if(FbCollector.untilPointer > FbCollector.sincePointer)
            {
                /* collect posts and comments */
                String tempSince = Util.getDateTimeUtc(FbCollector.sincePointer);
                String tempUntil = Util.getDateTimeUtc(FbCollector.sincePointer + FbCollector.timeSlice);
                String url = getCollectUrl(tempSince, tempUntil);
                collect(url);
                for(String postId: postIds)
                {
                    CommentsCollector commentsCollector = new CommentsCollector(page.getUsername(), postId);
                    if(commentsCollector.isFetchRequired())
                    {
                        commentsCollector.collect();
                        commentsCount += commentsCollector.comments.size();
                    }
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
