package common;

import db.DbManager;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public Post(String postId)
    {
        this.id = postId;
        Connection connection = DbManager.getConnection();
        String query = "SELECT likes,comments,shares FROM Post WHERE id=?";
        try
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, postId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                this.likes = resultSet.getInt(1);
                this.comments = resultSet.getInt(2);
                this.shares = resultSet.getInt(3);
            }
            resultSet.close();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
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
        String jsonDir = Util.buildPath(getPage().getUsername(), "posts", getId());
        String path = jsonDir + "/" + (Config.crawlHistory ? Util.getCurDateTimeDir() + "_" : "") + "post.json";
        try
        {
            FileWriter writer = new FileWriter(path);
            getPost().writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println("failed to write json file " + path);
        }
    }

    public boolean postExists()
    {
        return DbManager.entryExists("Post", "id", getId());
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

        if(Config.crawlHistory)
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
            statement.setString(1, getMessage());
            statement.setString(2, Util.toDbDateTime(getUpdatedAt()));
            statement.setInt(3, getLikes());
            statement.setInt(4, getComments());
            statement.setInt(5, getShares());
            statement.setString(6, getId());
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
            statement.setString(4, Util.toDbDateTime(getCreatedAt()));
            statement.setString(5, Util.toDbDateTime(getUpdatedAt()));
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
            statement.setString(2, id);
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

    public Page getPage()
    {
        return page;
    }

    public JSONObject getPost()
    {
        return post;
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

    public int getComments()
    {
        return comments;
    }

    public int getShares()
    {
        return shares;
    }

    public String getUpdatedAt()
    {
        return updatedAt;
    }
}
