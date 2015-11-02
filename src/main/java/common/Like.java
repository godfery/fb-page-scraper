package common;

import org.json.simple.JSONObject;

public class Like
{
    private String postId;
    private String fromId;
    private String fromName;

    public Like(String postId, JSONObject likeJson)
    {
        this.postId = postId;
        fromId = likeJson.get("id").toString();
        fromName = likeJson.get("name").toString();
    }

    public void updateDb()
    {

    }

    public String getPostId()
    {
        return postId;
    }

    public String getFromId()
    {
        return fromId;
    }

    public String getFromName()
    {
        return fromName;
    }
}
