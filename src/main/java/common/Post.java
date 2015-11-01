package common;

import cmdline.FbCollector;
import db.DbManager;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Post
{
    private Page page;
    private JSONObject post;
    private String id;
    private String message;
    private String createdAt;
    private int likes;
    private int comments;
    private int shares;
    private String updatedAt;

    public Post(Page page, JSONObject postJson)
    {
        this.page = page;
        this.post = postJson;
        id = postJson.get("id").toString();
        message = null != postJson.get("message") ? postJson.get("message").toString() : null;
        createdAt = null != postJson.get("created_time") ? postJson.get("created_time").toString() : null;
        shares = getSharesCount(postJson);
        likes = getLikesCount(postJson);
        comments = getCommentsCount(postJson);
        updatedAt = null != postJson.get("updated_time") ? postJson.get("updated_time").toString() : null;
    }

    public static int getLikesCount(JSONObject post)
    {
        int likesCount = 0;
        JSONObject likesObject = (JSONObject) post.get("likes");
        if(null != likesObject)
        {
            JSONObject likesSummaryObject = (JSONObject) likesObject.get("summary");
            if(null != likesSummaryObject)
            {
                likesCount = Integer.parseInt(likesSummaryObject.get("total_count").toString());
            }
        }
        return likesCount;
    }

    public static int getCommentsCount(JSONObject post)
    {
        int commentsCount = 0;
        JSONObject commentsObject = (JSONObject) post.get("comments");
        if(null != commentsObject)
        {
            JSONObject commentsSummaryObject = (JSONObject) commentsObject.get("summary");
            if(null != commentsSummaryObject)
            {
                commentsCount = Integer.parseInt(commentsSummaryObject.get("total_count").toString());
            }
        }
        return commentsCount;
    }

    public static int getSharesCount(JSONObject post)
    {
        int sharesCount = 0;
        JSONObject sharesObject = (JSONObject) post.get("shares");
        if(null != sharesObject && null != sharesObject.get("count"))
        {
            sharesCount = Integer.parseInt(sharesObject.get("count").toString());
        }
        return sharesCount;
    }

    public void writeJson()
    {
        String jsonDir = Util.buildPath(page.getUsername(), "posts", id);
        String path = jsonDir + "/" + Util.getCurDateTimeDir() + "_post.json";
        try
        {
            FileWriter writer = new FileWriter(path);
            post.writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println("failed to write json file " + path);
        }
    }

    public boolean postExists()
    {
        return DbManager.entryExists("Post", "id", id);
    }

    public void updateDb()
    {
        if(postExists())
        {
            updatePost();
        }
        else
        {
            insertPost();
        }

        if(Config.postCrawl)
        {
            insertPostCrawl();
        }
    }

    private void updatePost()
    {
        Connection connection = DbManager.getConnection();
        try
        {
            String query = "UPDATE Post "
                    + "SET message=?,updated_at=?,likes=?,comments=?,shares=? "
                    + "WHERE id=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, message);
            statement.setString(2, Util.toDbDateTime(updatedAt));
            statement.setInt(3, likes);
            statement.setInt(4, comments);
            statement.setInt(5, shares);
            statement.setString(6, id);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void insertPost()
    {
        Connection connection = DbManager.getConnection();
        String query = "INSERT INTO Post "
                + "(id,page_id,message,created_at,updated_at,likes,comments,shares) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, id);
            statement.setString(2, page.getId());
            statement.setString(3, message);
            statement.setString(4, Util.toDbDateTime(createdAt));
            statement.setString(5, Util.toDbDateTime(updatedAt));
            statement.setInt(6, likes);
            statement.setInt(7, comments);
            statement.setInt(8, shares);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void insertPostCrawl()
    {
        Connection connection = DbManager.getConnection();
        String query = "INSERT INTO PostCrawl "
                + "(crawl_date,post_id,likes,comments,shares) "
                + "VALUES (?,?,?,?,?)";
        try
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, Util.getDbDateTimeUtc());
            statement.setString(2, page.getId());
            statement.setInt(3, likes);
            statement.setInt(4, comments);
            statement.setInt(5, shares);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}
