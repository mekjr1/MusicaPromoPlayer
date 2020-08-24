
package net.nmtss.mp.models.album;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FileDownload implements Serializable
{

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("ids")
    @Expose
    private int ids;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("mp3")
    @Expose
    private String mp3;
    @SerializedName("artist")
    @Expose
    private String artist;
    @SerializedName("poster")
    @Expose
    private boolean poster;
    private final static long serialVersionUID = -5381674747762726517L;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIds() {
        return ids;
    }

    public void setIds(int ids) {
        this.ids = ids;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMp3() {
        return mp3;
    }

    public void setMp3(String mp3) {
        this.mp3 = mp3;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public boolean isPoster() {
        return poster;
    }

    public void setPoster(boolean poster) {
        this.poster = poster;
    }

}
