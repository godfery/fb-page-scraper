package common;

import cmdline.FbCollector;
import db.DbManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CommentsCollector
{
    private String page;
    private String postId;
    public JSONArray comments = new JSONArray();
    public static long tempCount = 0;

    public CommentsCollector(String page, String postId)
    {
        this.page = page;
        this.postId = postId;
    }

    public void collect()
    {
        if(FbCollector.commentsCount - tempCount > 10000)
        {
            Util.sleep(300);
            tempCount = FbCollector.commentsCount;
        }

        String url = Config.baseUrl + "/" + postId + "/comments";
        url += "?access_token=" + Config.accessToken;
        url += "&fields=id,message,created_time,like_count,from";

        collect(url);

        if(!comments.isEmpty())
        {
            if(Config.collectJson)
            {
                JSONObject obj = new JSONObject();
                obj.put("data", comments);
                writeCommentsJson(obj);
            }

            List<Comment> allComments = new ArrayList<Comment>();
            Iterator itr = comments.iterator();
            while (itr.hasNext())
            {
                JSONObject commentJson = (JSONObject) itr.next();
                Comment comment = new Comment(postId, commentJson);
                allComments.add(comment);
            }
            updateDb(allComments);
        }
    }

    private void collect(String url)
    {
        JSONObject commentsJson = Util.getJson(url);
        if(null != commentsJson)
        {
            JSONArray commentsData = (JSONArray) commentsJson.get("data");
            comments.addAll(commentsData);
            JSONObject paging = (JSONObject) commentsJson.get("paging");
            if(null != paging && null != paging.get("next"))
            {
                collect(paging.get("next").toString());
            }
        }
        else
        {
            System.err.println("reading comments failed for url: " + url);
        }
    }

    private void writeCommentsJson(JSONObject commentsJson)
    {
        String jsonDir = Util.buildPath(page, "posts", postId);
        String path = jsonDir + "/comments.json";
        try
        {
            FileWriter writer = new FileWriter(path);
            commentsJson.writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println("failed to write json file " + path);
        }
    }

    public boolean isFetchRequired()
    {
        return getCommentsCount() < new Post(postId).getComments();
    }

    public int getCommentsCount()
    {
        int count = 0;
        Connection connection = DbManager.getConnection();
        String query = "SELECT COUNT(*) AS Count FROM Comment WHERE post_id=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            statement = connection.prepareStatement(query);
            statement.setString(1, postId);
            resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                count = resultSet.getInt(1);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(null != resultSet) try { resultSet.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != statement) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return count;
    }

    public void updateDb(List<Comment> comments)
    {
        List<Comment> insertComments = new ArrayList<Comment>();
        List<Comment> updateComments = new ArrayList<Comment>();
        Connection connection = DbManager.getConnection();
        String query = "SELECT id FROM Comment WHERE id=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            statement = connection.prepareStatement(query);
            for(Comment comment: comments)
            {
                statement.setString(1, comment.getId());
                resultSet = statement.executeQuery();
                if(resultSet.next())
                {
                    if(null == resultSet.getString(1))
                    {
                        insertComments.add(comment);

                        if(++FbCollector.commentsCount % 1000 == 0)
                        {
                            System.out.println(Util.getDbDateTimeEst() + " Fetched " + FbCollector.commentsCount + " comments");
                        }
                    }
                    else
                    {
                        updateComments.add(comment);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(null != resultSet) try { resultSet.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != statement) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        insertComments(insertComments);
        updateComments(updateComments);
    }

    public void insertComments(List<Comment> comments)
    {
        final int batchSize = 100;
        int count = 0;
        Connection connection = DbManager.getConnection();
        String query = "INSERT INTO Comment "
                + "(id, post_id, message, created_at, from_id, from_name, likes) "
                + "VALUES (?,?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try
        {
            statement = connection.prepareStatement(query);
            for(Comment comment: comments)
            {
                statement.setString(1, comment.getId());
                statement.setString(2, comment.getPostId());
                statement.setString(3, comment.getMessage());
                statement.setString(4, Util.toDbDateTime(comment.getCreatedAt()));
                statement.setString(5, comment.getFromId());
                statement.setString(6, comment.getFromName());
                statement.setInt(7, comment.getLikes());
                statement.addBatch();

                if(++count % batchSize == 0)
                {
                    statement.executeBatch();
                }
            }
            statement.executeBatch();
        }
        catch (SQLException e)
        {
            System.err.println("failed to insert comments for post: " + postId);
        }
        finally
        {
            if(null != statement) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void updateComments(List<Comment> comments)
    {
        final int batchSize = 100;
        int count = 0;
        Connection connection = DbManager.getConnection();
        String query = "UPDATE Comment SET message=?,likes=? WHERE id=?";
        PreparedStatement statement = null;
        try
        {
            statement = connection.prepareStatement(query);
            for(Comment comment: comments)
            {
                statement.setString(1, comment.getMessage());
                statement.setInt(2, comment.getLikes());
                statement.setString(3, comment.getId());
                statement.addBatch();

                if(++count % batchSize == 0)
                {
                    statement.executeBatch();
                }
            }
            statement.executeBatch();
        }
        catch (SQLException e)
        {
            System.err.println("failed to insert comments for post: " + postId);
        }
        finally
        {
            if(null != statement) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
