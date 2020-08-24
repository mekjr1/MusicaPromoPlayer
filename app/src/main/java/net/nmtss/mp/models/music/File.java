
package net.nmtss.mp.models.music;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class File implements Serializable
{

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("ids")
    @Expose
    private Integer ids;
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
    private String poster;
    private final static long serialVersionUID = 9199503438811761605L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIds() {
        return ids;
    }

    public void setIds(Integer ids) {
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

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

}
