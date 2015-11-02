package common;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.util.Iterator;

public class LikesCollector
{
    private String page;
    private String postId;
    public JSONArray likes = new JSONArray();

    public LikesCollector(String page, String postId)
    {
        this.page = page;
        this.postId = postId;
    }

    public void collect()
    {
        String url = Config.baseUrl + "/" + postId + "/likes";
        url += "?access_token=" + Config.accessToken;
        url += "&fields=id,name";

        collect(url);

        if(!likes.isEmpty())
        {
            if(Config.collectJson)
            {
                JSONObject obj = new JSONObject();
                obj.put("data", likes);
                writeLikesJson(obj);
            }

            Iterator itr = likes.iterator();
            while (itr.hasNext())
            {
                JSONObject likeJson = (JSONObject) itr.next();
                Like like = new Like(postId, likeJson);
                like.updateDb();
            }
        }
    }

    private void collect(String url)
    {
        JSONObject likesJson = Util.getJson(url);
        if(null != likesJson)
        {
            JSONArray likesData = (JSONArray) likesJson.get("data");
            likes.addAll(likesData);
            JSONObject paging = (JSONObject) likesJson.get("paging");
            if(null != paging && null != paging.get("next"))
            {
                collect(paging.get("next").toString());
            }
        }
        else
        {
            System.err.println("reading likes failed for url: " + url);
        }
    }

    private void writeLikesJson(JSONObject likesJson)
    {
        String jsonDir = Util.buildPath(page, "posts", postId);
        String path = jsonDir + "/likes.json";
        try
        {
            FileWriter writer = new FileWriter(path);
            likesJson.writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println("failed to write json file " + path);
        }
    }
}
