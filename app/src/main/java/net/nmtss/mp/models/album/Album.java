
package net.nmtss.mp.models.album;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.nmtss.mp.models.music.Preview;


public class Album implements Serializable
{
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("featured_img")
    @Expose
    private String featuredImg;
    @SerializedName("external_link")
    @Expose
    private Object externalLink;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("attachment")
    @Expose
    private String attachment;
    @SerializedName("preview")
    @Expose
    private Preview preview = null;
    @SerializedName("bundled")
    @Expose
    private List<Bundled> bundled = null;
    @SerializedName("artists")
    @Expose
    private List<Artist> artists = null;
    private final static long serialVersionUID = 4282773094308056967L;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFeaturedImg() {
        return featuredImg;
    }

    public void setFeaturedImg(String featuredImg) {
        this.featuredImg = featuredImg;
    }

    public Object getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(Object externalLink) {
        this.externalLink = externalLink;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public Preview getPreview() {
        return preview;
    }

    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    public List<Bundled> getBundled() {
        return bundled;
    }

    public void setBundled(List<Bundled> bundled) {
        this.bundled = bundled;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }


}
