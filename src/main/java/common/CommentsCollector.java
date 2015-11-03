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

    public CommentsCollector(String page, String postId)
    {
        this.page = page;
        this.postId = postId;
    }

    public void collect()
    {
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

            Iterator itr = comments.iterator();
            while (itr.hasNext())
            {
                JSONObject commentJson = (JSONObject) itr.next();
                Comment comment = new Comment(postId, commentJson);
                comment.updateDb();
            }
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
        try
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, postId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                count = resultSet.getInt(1);
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
        return count;
    }
}
