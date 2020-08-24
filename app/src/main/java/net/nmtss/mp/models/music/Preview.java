
package net.nmtss.mp.models.music;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Preview implements Serializable
{

    @SerializedName("mp3")
    @Expose
    private String mp3;
    private final static long serialVersionUID = 5851983983331310367L;

    public String getMp3() {
        return mp3;
    }

    public void setMp3(String mp3) {
        this.mp3 = mp3;
    }

}
