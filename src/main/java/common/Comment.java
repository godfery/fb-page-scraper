package common;

import db.DbManager;
import org.json.simple.JSONObject;

public class Comment
{
    private String postId;
    private String id;
    private String message;
    private String createdAt;
    private int likes;
    private String fromId;
    private String fromName;

    public Comment(String postId, JSONObject comment)
    {
        this.postId = postId;
        id = comment.get("id").toString();
        message = null != comment.get("message") ? comment.get("message").toString() : null;
        createdAt = null != comment.get("created_time") ? comment.get("created_time").toString() : null;
        likes = null != comment.get("like_count") ? Integer.parseInt(comment.get("like_count").toString()) : 0;
        JSONObject from = (JSONObject) comment.get("from");
        if(null != from)
        {
            fromId = null != from.get("id") ? from.get("id").toString() : "";
            fromName = null != from.get("name") ? from.get("name").toString() : "";
        }
    }

    public boolean commentExists()
    {
        return DbManager.entryExists("Comment", "id", getId());
    }

    public String getPostId()
    {
        return postId;
    }

    public String getId()
    {
        return id;
    }

    public String getMessage()
    {
        return message;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public int getLikes()
    {
        return likes;
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
