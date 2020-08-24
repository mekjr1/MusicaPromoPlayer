package net.nmtss.mp.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.nmtss.mp.api.ApiUtils;
import net.nmtss.mp.api.MpEndpoint;
import net.nmtss.mp.models.album.Album;
import net.nmtss.mp.models.music.Music;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by himanshu on 27/2/19.
 */

interface OnFetchMusicListener
{
    public void onMusicsFetched(ArrayList<Music> allSongs);
    public void onMusicError(String error);
}
interface OnFetchTrendingListener
{
    public void onTrendingFetched(ArrayList<Music> allSongs);
    public void onTrendingError(String error);
}
interface OnFetchAlbumsListener
{
    public void onAlbumsFetched(ArrayList<Album> allSongs);
    public void onAlbumsError(String error);
}

public class SplashScreen extends AppCompatActivity implements OnFetchTrendingListener, OnFetchAlbumsListener, OnFetchMusicListener{

    private MpEndpoint mPservice;
    private OnFetchTrendingListener mOnFetchTrendingListener;
    private OnFetchAlbumsListener mOnFetchAlbumsListener;
    private OnFetchMusicListener mOnFetchMusicListener;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    //OnFetchUrlsListener onFetchUrlsListener


    //private static int SPLASH_TIME = 4000; //This is 4 seconds
    public ArrayList<String> songsArray = new ArrayList<String>();
    public static ArrayList<String> songsName = new ArrayList<String>();
    private boolean songs_fetched=false;
    Button btReload;
    public ArrayList<Music> music_list ;
    public ArrayList<Music> trending_list ;
    public ArrayList<Album> albums_list ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPservice = ApiUtils.getMpEndpoint(this,getString(R.string.apibaseurl));

        //preferences
        preferences = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        editor = preferences.edit();

        if(isNetworkAvailable()) {
            setContentView(R.layout.activity_splash_screen);
            getWindow().getDecorView().setBackgroundColor(Color.BLACK);
            //Since there is no fixed time to start the activity, splash screen should be done as
            //Async task
            getTrendingMusic();
        }
        else {
            setContentView(R.layout.activity_no_internet);
            btReload = findViewById(R.id.idReloadButton);

            btReload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isNetworkAvailable()) {
                        setContentView(R.layout.activity_splash_screen);
                        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
                        //Since there is no fixed time to start the activity, splash screen should be done as
                        //Async task
                        
                        getTrendingMusic();
                    }
                }
            });
        }
    }

    private void getTrendingMusic() {
        mOnFetchTrendingListener = SplashScreen.this;
        Call<List<Music>> call = mPservice.getTrendingMusics();
        call.enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {
                if(response.isSuccessful()){
                    mOnFetchTrendingListener.onTrendingFetched((ArrayList<Music>) response.body());
                    System.out.println("Step one");
                }
            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                System.out.println("Not step one");
                mOnFetchTrendingListener.onTrendingError("Algo errado nao esta certo");
                System.out.println(call.request().url());
                t.printStackTrace();
            }
        });
    }
    private void getAlbums() {
        mOnFetchAlbumsListener = SplashScreen.this;
        Call<List<Album>> call = mPservice.getAlbums();
        call.enqueue(new Callback<List<Album>>() {
            @Override
            public void onResponse(Call<List<Album>> call, Response<List<Album>> response) {
                if(response.isSuccessful()){
                    mOnFetchAlbumsListener.onAlbumsFetched((ArrayList<Album>) response.body());
                    System.out.println("Step two");
                }
            }

            @Override
            public void onFailure(Call<List<Album>> call, Throwable t) {
                System.out.println("Not step one");
                mOnFetchAlbumsListener.onAlbumsError("Algo errado nao esta certo");
                System.out.println(call.request().url());
                t.printStackTrace();
            }
        });
    }
    private void getMusics() {
        mOnFetchMusicListener = SplashScreen.this;
        Call<List<Music>> call = mPservice.getMusics();
        call.enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {
                if(response.isSuccessful()){
                    mOnFetchMusicListener.onMusicsFetched((ArrayList<Music>) response.body());
                    System.out.println("Step three");
                }
            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                System.out.println("Not step one");
                mOnFetchMusicListener.onMusicError("Algo errado nao esta certo");
                System.out.println(call.request().url());
                t.printStackTrace();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onTrendingFetched(ArrayList<Music> allSongs) {
        this.trending_list=allSongs;
        getAlbums();

    }

    @Override
    public void onTrendingError(String error) {

    }



    @Override
    public void onAlbumsFetched(ArrayList<Album> allAlbums) {
        this.albums_list=allAlbums;
        getMusics();

    }

    @Override
    public void onAlbumsError(String error) {

    }
    @Override
    public void onMusicsFetched(ArrayList<Music> allSongs) {
        this.music_list=allSongs;
        System.out.println("prep to launch");
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        i.putExtra("alltrends", trending_list);
        i.putExtra("allsongs", music_list);
        i.putExtra("allalbums", albums_list);
        System.out.println(allSongs.size());
        startActivity(i);
        finish();


    }

    @Override
    public void onMusicError(String error) {

    }
}
