package net.nmtss.mp.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import net.nmtss.mp.api.ApiUtils;
import net.nmtss.mp.api.MpEndpoint;
import net.nmtss.mp.models.album.Bundled;
import net.nmtss.mp.models.music.Music;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by himanshu on 26/2/19.
 */

public class MoreMusics extends AppCompatActivity implements PaginationAdapterCallback, SongsAdapterRecyclerView_Vertical.OnMoreSongItemClickListner {
    ProgressDialog progress;
    ArrayList<Music> allSongs;
    DividerItemDecoration itemDecorator;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.

    private static final int PAGE_START = 1;
    private static final int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;
    SongsAdapterRecyclerView_Vertical adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView rv;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError;
    SwipeRefreshLayout swipeRefreshLayout;

    private static final String TAG = "MoreSongsActivity";
    private MpEndpoint mPservice;
    ImageView mPlayerMusicCover;
    ImageView mPlayerControl;
    TextView mPlayerCurrentMusicTitle;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        itemDecorator = new DividerItemDecoration(MoreMusics.this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(MoreMusics.this, R.drawable.divider));
        mPservice = ApiUtils.getMpEndpoint(this, getString(R.string.apibaseurl));

        //MediaPlayer items
       mPlayerMusicCover = findViewById(R.id.id_selected_track_image);
       mPlayerControl = findViewById(R.id.id_player_control);
       mPlayerCurrentMusicTitle = findViewById(R.id.id_selected_track_title);
       mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });
        MusicManager.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {  mPlayerControl.setImageResource(R.drawable.icon_play);
            }
        });


        rv = findViewById(R.id.idAllSongs);
        progressBar = findViewById(R.id.main_progress);
        errorLayout = findViewById(R.id.error_layout);
        btnRetry = findViewById(R.id.error_btn_retry);
        txtError = findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = findViewById(R.id.main_swiperefresh);

        adapter = new SongsAdapterRecyclerView_Vertical(this,this);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapter);

        rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                // mocking network delay for API call
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextPage();
                    }
                }, 1000);isLoading = true;

            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        //if music is playing from previous activity show the player
        if(MusicManager.isPlaying==true)
            mPlayerControl.setImageResource(R.drawable.icon_pause);
        else if(!mPlayerCurrentMusicTitle.getText().toString().matches(""))
            mPlayerControl.setImageResource(R.drawable.icon_play);

        mPlayerCurrentMusicTitle.setText(MusicManager.current_song_name);
        if(!MusicManager.current_song_icon_url.matches(""))
            Picasso.get().load(MusicManager.current_song_icon_url).into(mPlayerMusicCover);
        //end of showing player

        loadFirstPage();

    }

    private void togglePlayPause() {
        if (MusicManager.isPlaying == true) {
            MusicManager.player.pause();
            MusicManager.isPlaying = false;
            mPlayerControl.setImageResource(R.drawable.icon_play);
        } else {
            MusicManager.isPlaying = true;
            MusicManager.player.start();
            mPlayerControl.setImageResource(R.drawable.icon_pause);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(MusicManager.isPlaying==true)
            mPlayerControl.setImageResource(R.drawable.icon_pause);
        else if(!mPlayerCurrentMusicTitle.getText().toString().matches(""))
            mPlayerControl.setImageResource(R.drawable.icon_play);

        mPlayerCurrentMusicTitle.setText(MusicManager.current_song_name);
        if(!MusicManager.current_song_icon_url.matches(""))
            Picasso.get().load(MusicManager.current_song_icon_url).into(mPlayerMusicCover);

    }

    /**
     * Performs a Retrofit call to the Music Promo API.
     */
    private Call<List<Music>> getLatestMusics() { //2
        Log.d(TAG,"getMusics:");
        return mPservice.getMusics(currentPage);
    }

    /*
     * Extracts List<Result> from response
     */
    private List<Music> fecthResults(Response<List<Music>> response) {
        List<Music> musicList = new ArrayList<Music>();
        for (Music m: response.body()) {
            if (m!=null){
                musicList.add(processMusic(m));
            }

        }
        return musicList;
    }

    private Music processMusic(Music m) {
        Log.d(TAG, "Processing Music"+m.getName());
        if(m.getPreview().getMp3().isEmpty()){
            m.getPreview().setMp3("NoMusic.mp3");
        }
        if(m.getFeaturedImg().isEmpty()){
            m.setFeaturedImg("https://i2.wp.com/www.musicapromo.net/wp-content/uploads/2015/09/MUSICAPROMO.jpg");
        }
        m.setContent(Jsoup.parse(m.getContent()).text().trim());
        m.setName(Jsoup.parse(m.getName()).text().trim());

        return m;
    }

    private void loadFirstPage() {


        Log.d(TAG, "loadingFirstPage: ");
        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();
        currentPage = PAGE_START;
        getLatestMusics().enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {
                List<Music> musics = fecthResults(response);
                Log.d(TAG,"getLatestMusic.enquee"+ musics.size());
                progressBar.setVisibility(View.GONE);

                adapter.addAll(musics);
                if (currentPage <= TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;

            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
                Log.d(TAG,"getLatestMusic.enque errpr");
            }
        });
    }


    private void loadNextPage() {
        Log.d(TAG, "loadingNextPage:" + currentPage);
        getLatestMusics().enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {
                adapter.removeLoadingFooter();
                isLastPage = false;
                List<Music> musics = fecthResults(response);
                adapter.addAll(musics);
                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });

    }

    public void playMusic(Music current){

        Log.d(TAG,"Preparing to Play Music");
        if ( current.getFeaturedImg()!=null && !current.getFeaturedImg().isEmpty()){
            mPlayerControl.setImageResource(R.drawable.icon_pause);
            mPlayerCurrentMusicTitle.setText(Jsoup.parse(current.getName()).text().trim());

            MusicManager.current_song_icon_url = current.getFeaturedImg();
            MusicManager.current_song_name= current.getName();
            Picasso.get().load(current.getFeaturedImg()).into(mPlayerMusicCover);
            MusicManager.SoundPlayer(this, current.getPreview().getMp3(), progress);

        }
        else
        {

        }

    }


    @Override
    public void onSongTitleClicked(int position) {
        Music music = adapter.getMusics().get(position);
playMusic(music);

    }

    @Override
    public void onSongImageClick(int position) {

        Music music = adapter.getMusics().get(position);
        playMusic(music);
    }

    @Override
    public void onSongDescriptionClicked(int position) {

    }

    @Override
    public void onSongYearClicked(int position) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_more_songs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                // Signal SwipeRefreshLayout to start the progress indicator
                swipeRefreshLayout.setRefreshing(true);
                doRefresh();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Triggers the actual background refresh via the {@link SwipeRefreshLayout}
     */
    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (getLatestMusics().isExecuted())
            getLatestMusics().cancel();

        // TODO: Check if data is stale.
        //  Execute network request if cache is expired; otherwise do not update data.
        adapter.getMusics().clear();
        adapter.notifyDataSetChanged();
        loadFirstPage();


    }
    @Override
    public void retryPageLoad() {
        loadNextPage();
    }


    /**
     * @param throwable required for {@link #fetchErrorMessage(Throwable)}
     * @return
     */
    private void showErrorView(Throwable throwable) {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    /**
     * @param throwable to identify the type of error
     * @return appropriate error message
     */
    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }

        return errorMsg;
    }

    // Helpers -------------------------------------------------------------------------------------


    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Remember to add android.permission.ACCESS_NETWORK_STATE permission.
     *
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}

