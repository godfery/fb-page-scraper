package common;

import cmdline.FbCollector;
import db.DbManager;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Page
{
    private JSONObject json;
    private String id;
    private String username;
    private String name;
    private int likes;
    private int talkingAbout;
    private int checkins;
    private String website;
    private String link;
    private String category;
    private String affiliation;
    private String about;

    public Page(JSONObject pageJson)
    {
        json = pageJson;
        id = pageJson.get("id").toString();
        username = null != pageJson.get("username") ? pageJson.get("username").toString() : null;
        name =  null != pageJson.get("name") ? pageJson.get("name").toString() : null;
        likes = null != pageJson.get("likes") ? Integer.parseInt(pageJson.get("likes").toString()) : 0;
        talkingAbout = null != pageJson.get("talking_about_count") ? Integer.parseInt(pageJson.get("talking_about_count").toString()) : 0;
        checkins = null != pageJson.get("checkins") ? Integer.parseInt(pageJson.get("checkins").toString()) : 0;
        website = null != pageJson.get("website") ? pageJson.get("website").toString(): null;
        link = null != pageJson.get("link")? pageJson.get("link").toString() : null;
        category = null != pageJson.get("category") ? pageJson.get("category").toString() : null;
        affiliation = null != pageJson.get("affiliation") ? pageJson.get("affiliation").toString() : null;
        about = null != pageJson.get("about") ? pageJson.get("about").toString() : null;
    }

    public Page(String username)
    {
        this.username = username;
        this.id = DbManager.getFieldValue("Page", "id", "username", username);
    }

    public void writeJson()
    {
        String jsonDir = Util.buildPath(username, "page");
        String path = jsonDir + "/" + (Config.pageCrawl ? Util.getCurDateTimeDir() + "_" : "") + username + ".json";
        try
        {
            FileWriter writer = new FileWriter(path);
            json.writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println("failed to write json file " + path);
        }
    }

    public boolean pageExists()
    {
        return DbManager.entryExists("Page", "id", id);
    }

    public void updateDb()
    {
        if(pageExists())
        {
            if(Config.collectOnce || (FbCollector.loopIndex % 100) == 0)
            {
                updatePage();
            }
            else
            {
                updatePageStats();
            }
        }
        else
        {
            insertPage();
        }

        if(Config.pageCrawl)
        {
            insertPageCrawl();
        }
    }

    private void insertPage()
    {
        Connection connection = DbManager.getConnection();
        String query = "INSERT INTO Page "
                + "(id,username,name,likes,talking_about,checkins,website,link,category,affiliation,about) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, id);
            statement.setString(2, username);
            statement.setString(3, name);
            statement.setInt(4, likes);
            statement.setInt(5, talkingAbout);
            statement.setInt(6, checkins);
            statement.setString(7, website);
            statement.setString(8, link);
            statement.setString(9, category);
            statement.setString(10, affiliation);
            statement.setString(11, about);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updatePage()
    {
        Connection connection = DbManager.getConnection();
        String query = "UPDATE Page "
                + "SET username=?,name=?,likes=?,talking_about=?,checkins=?,website=?,link=?,category=?,affiliation=?,about=? "
                + "WHERE id=?";
        try
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, name);
            statement.setInt(3, likes);
            statement.setInt(4, talkingAbout);
            statement.setInt(5, checkins);
            statement.setString(6, website);
            statement.setString(7, link);
            statement.setString(8, category);
            statement.setString(9, affiliation);
            statement.setString(10, about);
            statement.setString(11, id);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updatePageStats()
    {
        Connection connection = DbManager.getConnection();
        try
        {
            String query = "UPDATE Page SET likes=?, talking_about=? WHERE id=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, likes);
            statement.setInt(2, talkingAbout);
            statement.setString(3, id);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void insertPageCrawl()
    {
        Connection connection = DbManager.getConnection();
        try
        {
            String query = "INSERT INTO PageCrawl (crawl_date,page_id,likes,talking_about) VALUES (?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, Util.getDbDateTimeUtc());
            statement.setString(2, id);
            statement.setInt(3, likes);
            statement.setInt(4, talkingAbout);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public int getLikes() {
        return likes;
    }

    public int getTalkingAbout() {
        return talkingAbout;
    }

    public int getCheckins() {
        return checkins;
    }

    public String getWebsite() {
        return website;
    }

    public String getLink() {
        return link;
    }

    public String getCategory() {
        return category;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public String getAbout() {
        return about;
    }
}
