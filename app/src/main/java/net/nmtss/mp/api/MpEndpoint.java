package net.nmtss.mp.api;

import net.nmtss.mp.models.album.Album;
import net.nmtss.mp.models.music.Music;

import retrofit2.Call;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MpEndpoint {

    @GET("musics")
    Call <List<Music>> getMusics();

    @GET("trends")
    Call<List<Music>> getTrendingMusics();

    @GET("albums")
    Call<List<Album>> getAlbums();
    @GET("musics")
    Call < List<Music> > getMusics(@Query("page") int pageIndex);
}
