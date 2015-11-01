package common;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;

public class LikesCollector
{
    private String postId;
    private String postDir;
    public JSONArray likes = new JSONArray();
    public static final String likesFields = "id,name";

    public LikesCollector(String postId, String postDir)
    {
        this.postDir = postDir;
        this.postId = postId;
    }

    public void collect()
    {
        JSONObject json = Util.getJson(Config.baseUrl + "/" + postId + "/likes?limit=100&fields=" + likesFields + "&access_token=" + Config.accessToken);

        while (null != json)
        {
            likes.addAll((JSONArray) json.get("data"));
            JSONObject paging = (JSONObject) json.get("paging");
            if(null != paging && null != paging.get("next"))
            {
                json = Util.getJson(paging.get("next").toString());
            }
            else
            {
                json = null;
            }
        }

        if(!likes.isEmpty())
        {
            JSONObject obj = new JSONObject();
            obj.put("data", likes);
            writeJson(postDir + "/" + postId + "_likes.json", obj);
        }
    }

    private void writeJson(String path, JSONObject jsonObject)
    {
        try
        {
            FileWriter writer = new FileWriter(path);
            jsonObject.writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
