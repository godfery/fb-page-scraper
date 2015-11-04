package common;

import cmdline.FbCollector;
import db.DbManager;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
        return DbManager.entryExists("Comment", "id", id);
    }

    public void updateDb()
    {
        if(commentExists())
        {
            updateComment();
        }
        else
        {
            insertComment();
            if(++FbCollector.commentsCount % 1000 == 0)
            {
                System.out.println("Fetched " + FbCollector.commentsCount + " new comments");
                FbCollector.commentsCount = 0;
            }
        }
    }

    private void insertComment()
    {
        Connection connection = DbManager.getConnection();
        String query = "INSERT INTO Comment "
                + "(id, post_id, message, created_at, from_id, from_name, likes) "
                + "VALUES (?,?,?,?,?,?,?)";
        try
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, id);
            statement.setString(2, postId);
            statement.setString(3, message);
            statement.setString(4, Util.toDbDateTime(createdAt));
            statement.setString(5, fromId);
            statement.setString(6, fromName);
            statement.setInt(7, likes);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e)
        {
            System.err.println("failed to insert comments for post: " + postId);
        }
        try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateComment()
    {
        Connection connection = DbManager.getConnection();
        String query = "UPDATE Comment SET message=?,likes=? WHERE id=?";
        try
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, message);
            statement.setInt(2, likes);
            statement.setString(3, id);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e)
        {
            System.err.println("failed to update comments for post: " + postId);
        }
        try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}
