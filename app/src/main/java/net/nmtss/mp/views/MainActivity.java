package net.nmtss.mp.views;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.View;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.miguelcatalan.materialsearchview.MaterialSearchView;

import net.nmtss.mp.api.ApiUtils;
import net.nmtss.mp.api.MpEndpoint;
import net.nmtss.mp.models.album.Album;
import net.nmtss.mp.models.music.Music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements PaginationAdapterCallback, MaterialSearchView.SearchViewListener, MaterialSearchView.OnQueryTextListener,SongsAdapterRecyclerView.OnSongClickListener, AlbumAdapterRecyclerView.OnAlbumClickListener, AlbumAdapterRecyclerView_Vertical.OnAlbumClickListenerMore{
    Toolbar searchToolbar;
    MaterialSearchView materialSearchView;
    TextView tvSearchAlbum, tvSearchLyrics, tvSearchSatsang;
    ArrayList<String> searchViewSatsangNameResult = new ArrayList<>();
    ArrayList<String> searchViewSatsangUrlResult = new ArrayList<>();
    boolean doubleBackToExitPressedOnce = false;

    TextView tvMoreSongs, tvMoreAlbums, tvMoreLyrics;
    private ImageView mPlayerControl, mSelectedTrackImage;
    private TextView mSelectedTrackTitle;

    Toolbar toolbar;
    int NO_OF_ALBUMS=5;
    int pos=0;
    ProgressDialog progress;
    RecyclerView songsList;


    public boolean initialStage=true;
    public boolean playPause = false;

    private final static int NUM_PAGES = 5;
    private ViewPager mViewPager, musicsViepager, localMusicViewpager, albumsViewPager;
    private List<ImageView> dots;
    RecyclerView albumsRecyclerView, localMusicsRecyclerview, onlineMusicsRecyclerview;
    AlbumAdapterRecyclerView albumsRecyclerViewAdapter;
    SongsAdapterRecyclerView localMusicsRecyclerviewAdapter;
    SongsAdapterRecyclerView onlineMusicsRecyclerviewAdapter;

    LinearLayout recentsBtn, queueBtn, playlistBtn, linearLayoutControlBottom;
    DividerItemDecoration itemDecorator;

    private String satsang_cover_image_url;

    private ArrayList<Album>  albums_list ;
    private ArrayList<String> song_urls;
    public ArrayList<Music> music_list, local_musics_list,song_names, trending_list ;
    private boolean musicIsLoading = false;
    private boolean isMusicLastPage = false;

    private static final int PAGE_START = 1;
    private static final int TOTAL_PAGES = 5;
    private int currentMusicPage= PAGE_START;
    private final String TAG="MainActivity";

    private MpEndpoint mPservice;

    ProgressDialog progress2;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError;
    private RecyclerView allAlbumList, allLocalMusicList, allOnlineMusicList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPservice = ApiUtils.getMpEndpoint(this, getString(R.string.apibaseurl));
        setContentView(R.layout.activity_main);
        //toolbar = findViewById(R.id.idToolbar);
        linearLayoutControlBottom = findViewById(R.id.idControlBottom);
        recentsBtn = findViewById(R.id.idRecents);
        queueBtn = findViewById(R.id.idQueue);
        playlistBtn = findViewById(R.id.idMyPlaylists);


        progressBar = findViewById(R.id.main_progress);
        errorLayout = findViewById(R.id.error_layout);

        if(errorLayout==null){
            Log.d(TAG, "Thing is null up in here");
        }
        btnRetry = findViewById(R.id.error_btn_retry);
        txtError = findViewById(R.id.error_txt_cause);

        searchToolbar = findViewById(R.id.idSearchToolbar);
        setSupportActionBar(searchToolbar);
        getSupportActionBar().setTitle("Music Player");

        //search labels
        materialSearchView = findViewById(R.id.idSearchView);
        tvSearchAlbum = findViewById(R.id.idSearchAlbumText);
        tvSearchLyrics = findViewById(R.id.idSearchLyricsText);
        tvSearchSatsang = findViewById(R.id.idSearchMusicText);

        //search views recyclers
        allAlbumList = findViewById(R.id.search_album_rv);
        allLocalMusicList = findViewById(R.id.search_local_musics_rv);
        allOnlineMusicList = findViewById(R.id.search_local_musics_rv);

        itemDecorator = new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.divider));


        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);

        localMusicsRecyclerview = findViewById(R.id.idRVLocal_music);
        //localMusicsRecyclerviewAdapter= new SongsAdapterRecyclerView(music_list,this);
        //localMusicsRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        //localMusicsRecyclerview.setItemAnimator(new DefaultItemAnimator());

        albumsRecyclerView = findViewById(R.id.idRVAlbums);
        albumsRecyclerViewAdapter= new AlbumAdapterRecyclerView(albums_list,this);
        albumsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        albumsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        onlineMusicsRecyclerview = findViewById(R.id.idRVSongs);
        onlineMusicsRecyclerviewAdapter= new SongsAdapterRecyclerView(this,this);
        onlineMusicsRecyclerview.setLayoutManager(linearLayoutManager);
        onlineMusicsRecyclerview.setItemAnimator(new DefaultItemAnimator());
        onlineMusicsRecyclerview.setAdapter(onlineMusicsRecyclerviewAdapter);

        onlineMusicsRecyclerview.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                Log.d(TAG, "Load More items");
                musicIsLoading = true;
                currentMusicPage += 1;

                // mocking network delay for API call
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextMusicPage();
                    }
                }, 1000);musicIsLoading = true;

            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isMusicLastPage;
            }

            @Override
            public boolean isLoading() {
                return musicIsLoading;
            }
        });



        music_list=(ArrayList<Music>) getIntent().getSerializableExtra("allsongs");
        trending_list =(ArrayList<Music>) getIntent().getSerializableExtra("alltrends");
        albums_list =(ArrayList<Album>) getIntent().getSerializableExtra("allalbums");
        Log.d(TAG,"Loading Musics");
        loadMusics(music_list, 1);

        //songsList = findViewById(R.id.idRVSongs);
       // songsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
       // songsList.setAdapter(new SongsAdapterRecyclerView(music_list, this));

        local_musics_list=new ArrayList<>();

        //loadMusics(music_list);
        loadAlbums(albums_list);
        loadTrendingMusic(trending_list);
        loadLocalMusic(local_musics_list);

        loadRecentSongs();
        loadQueueSongs();

        recentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Recents.class);
                startActivity(i);
            }
        });


        queueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, QueueSongs.class);
                startActivity(i);
            }
        });

        playlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PlayLists.class);
                startActivity(i);
            }
        });
        materialSearchView.setOnSearchViewListener(this);
        materialSearchView.setOnQueryTextListener(this);

       /* materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {

            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                tvSearchAlbum.setText("");
                tvSearchLyrics.setText("");
                tvSearchSatsang.setText("");
                searchViewSatsangNameResult.clear();
                searchViewSatsangUrlResult.clear();

                ArrayList<Music> lstFoundSatsang = new ArrayList<>();
                allSongsList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                //allSongsList.setAdapter(new SongsAdapterRecyclerView_Vertical(lstFoundSatsang, MainActivity.this));


            }
        });

        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText!=null && !newText.isEmpty() && newText.length()>=3)
                {
                    Log.d("query", newText);


                    ArrayList<Music> lstFoundSatsang = new ArrayList<>();
                    int idx = 0;
                    for(Music item:song_names)
                    {
                        if(item.getName().toLowerCase().contains(newText.toLowerCase()))
                        {
                            lstFoundSatsang.add(item);
                            searchViewSatsangNameResult.add(item.getName());
                            searchViewSatsangUrlResult.add(song_urls.get(idx));
                        }
                        idx++;
                    }
                    if(lstFoundSatsang.size()==0)
                        tvSearchSatsang.setText("No satsang found!");
                    else
                        tvSearchSatsang.setText("Ultimas Adicoes");
                    allSongsList.addItemDecoration(itemDecorator);
                    allSongsList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                   // allSongsList.setAdapter(new SongsAdapterRecyclerView_Vertical(lstFoundSatsang, MainActivity.this));



                }
                else
                {
                    // return default
                }
                return true;
            }
        });*/

        mSelectedTrackTitle = (TextView)findViewById(R.id.id_selected_track_title);
        mSelectedTrackImage = (ImageView)findViewById(R.id.id_selected_track_image);
        tvMoreSongs = findViewById(R.id.idMoreSongs);
        tvMoreAlbums = findViewById(R.id.idMoreAlbums);
        tvMoreLyrics = findViewById(R.id.idMoreLyrics);
        mPlayerControl = (ImageView)findViewById(R.id.id_player_control);


        song_names = new ArrayList<>();
        song_urls = new ArrayList<>();
        ArrayList<Music> temp_urls = (ArrayList<Music>) getIntent().getSerializableExtra("allsongs");




        for(int i=0;i<music_list.size();i++)
        {
            song_urls.add(music_list.get(i).getPreview().toString());
            Log.d("song url", song_urls.get(i));

            song_names.add(temp_urls.get(i));
        }



        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });
        MusicManager.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayerControl.setImageResource(R.drawable.icon_play);
            }
        });
        mSelectedTrackImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Player.class);

                i.putExtra("current_song", music_list.get(pos));
                i.putExtra("song_pos", pos);
                startActivity(i);
            }
        });


        tvMoreSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MoreMusics.class);
                i.putExtra("more_songs", music_list);

//                i.putParcelableArrayListExtra("albumModelArrayList", albumModelArrayList);
//                i.putParcelableArrayListExtra("albumLyricsModelArrayList", albumLyricsModelArrayList);
                startActivity(i);
            }
        });

        tvMoreAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MoreAlbums.class);
                i.putExtra("more_albums", albums_list);
                startActivity(i);
            }
        });

    }

    private void loadLocalMusic(ArrayList<Music> local_musics_list) {

    }

    private void loadTrendingMusic(ArrayList<Music> trending_list) {
        mViewPager = findViewById(R.id.idViewPager);
        SwipeAdapter swipeAdapter = new SwipeAdapter(this, trending_list);
        mViewPager.setAdapter(swipeAdapter);
        //System.out.println("swipe" + mViewPager.getAdapter());
        addDots();

    }

    private void loadAlbums(ArrayList<Album> albums_list) {
        //Allbums
        //albumsRecyclerView = new AlbumAdapterRecyclerView(albums_list,this);
        albumsRecyclerView = findViewById(R.id.idRVAlbums);
        albumsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        albumsRecyclerView.setAdapter(new AlbumAdapterRecyclerView(albums_list, this));

    }

    private void loadMusics(ArrayList<Music> music_list, int music_page) {

        Log.d(TAG,"LoadMusics "+ music_list.size());
        //hideErrorView();
        progressBar.setVisibility(View.GONE);

        onlineMusicsRecyclerviewAdapter.addAll(music_list);
        if (currentMusicPage <= TOTAL_PAGES) onlineMusicsRecyclerviewAdapter.addLoadingFooter();
        else isMusicLastPage = true;
    }

    private void loadNextMusicPage() {
        Log.d(TAG, "loadingNextPage:" + currentMusicPage);
        getLatestMusics().enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {
                onlineMusicsRecyclerviewAdapter.removeLoadingFooter();
                isMusicLastPage = false;
                List<Music> musics = fecthResults(response);
                onlineMusicsRecyclerviewAdapter.addAll(musics);
                if (currentMusicPage != TOTAL_PAGES) onlineMusicsRecyclerviewAdapter.addLoadingFooter();
                else isMusicLastPage = true;
            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                t.printStackTrace();
                onlineMusicsRecyclerviewAdapter.showRetry(true, fetchErrorMessage(t));
            }
        });

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

    private Call<List<Music>> getLatestMusics() { //2
        Log.d(TAG,"getMusics:");
        return mPservice.getMusics(currentMusicPage);
    }


    private void loadRecentSongs(){
        DatabaseHelper myDb2 = new DatabaseHelper(this);
        myDb2.createTable(CommonVariables.RECENT_TABLE_NAME);
        Cursor ress = myDb2.getAllSongs(CommonVariables.RECENT_TABLE_NAME);
        if(ress.getCount()!=0)
        {
            while (ress.moveToNext())
            {
                CommonVariables.recent_song_url.add(ress.getString(1));
                CommonVariables.recent_song_name.add(ress.getString(2));
                CommonVariables.recent_song_icon_url.add(ress.getString(3));
            }
        }
    }

    private void loadQueueSongs(){
        DatabaseHelper myDb2 = new DatabaseHelper(this);
        myDb2.createTable(CommonVariables.QUEUE_TABLE_NAME);
        Cursor ress = myDb2.getAllSongs(CommonVariables.QUEUE_TABLE_NAME);
        if(ress.getCount()!=0)
        {
            while (ress.moveToNext())
            {
                CommonVariables.queue_song_url.add(ress.getString(1));
                CommonVariables.queue_song_name.add(ress.getString(2));
                CommonVariables.queue_song_icon_url.add(ress.getString(3));
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem menuItem = menu.findItem(R.id.id_action_search);
        materialSearchView.setMenuItem(menuItem);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.click_back_exit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void addDots() {
        dots = new ArrayList<>();
        LinearLayout dotsLayout = (LinearLayout)findViewById(R.id.idDots);

        for(int i = 0; i < NUM_PAGES; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageDrawable(getResources().getDrawable(R.drawable.pager_dot_selected));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dotsLayout.addView(dot, params);

            dots.add(dot);
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectDot(position);
//                Toast.makeText(MainActivity.this, "Position : "+position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void selectDot(int idx) {
        Resources res = getResources();
        for(int i = 0; i < NUM_PAGES; i++) {
            int drawableId = (i==idx)?(R.drawable.pager_dot_not_selected):(R.drawable.pager_dot_selected);
            Drawable drawable = res.getDrawable(drawableId);
            dots.get(i).setImageDrawable(drawable);
        }
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
        else if(!mSelectedTrackTitle.getText().toString().matches(""))
            mPlayerControl.setImageResource(R.drawable.icon_play);

        mSelectedTrackTitle.setText(MusicManager.current_song_name);
        if(!MusicManager.current_song_icon_url.matches(""))
            Picasso.get().load(MusicManager.current_song_icon_url).into(mSelectedTrackImage);

    }


    @Override
    public void onSongClick(int position) {
        pos = position;
        Log.d(TAG,"Image Clicked on Position: "+position);
        Music current =onlineMusicsRecyclerviewAdapter.getItem(position);
        playMusic(current);
    }

    @Override
    public void onImageClick(int position) {
        pos = position;
        Log.d(TAG,"Image Clicked on Position: "+position);
        Music current =onlineMusicsRecyclerviewAdapter.getItem(position);
        playMusic(current);

    }

    public void playMusic(Music current){

        Log.d(TAG,"Preparing to Play Music");
        if ( current.getFeaturedImg()!=null && !current.getFeaturedImg().isEmpty()){
            mPlayerControl.setImageResource(R.drawable.icon_pause);
            linearLayoutControlBottom.setBackgroundColor(999999);
            mSelectedTrackTitle.setText(current.getName());
            MusicManager.current_song_icon_url = current.getFeaturedImg();
            MusicManager.current_song_name= current.getName();
            Picasso.get().load(current.getFeaturedImg()).into(mSelectedTrackImage);
            MusicManager.SoundPlayer(this, current.getPreview().getMp3(), progress);

        }
        else{

        }


    }


    @Override
    public void onAlbumTitleClick(int position) {
        System.out.println("debugging onAlbumTitleClick"+ albums_list.size());
        Intent i = new Intent(MainActivity.this, SongAlbum.class);
        i.putExtra("album_song_one_item", albums_list.get(position));
        startActivity(i);
    }

    @Override
    public void onAlbumImageClick(int position) {
        System.out.println("debugging onAlbumImageClick"+ albums_list.size());
        Intent i = new Intent(MainActivity.this, SongAlbum.class);
        i.putExtra("album_song_one_item", albums_list.get(position));
        startActivity(i);
    }

    @Override
    public void onAlbumTitleClickMore(int position) {
        Intent i = new Intent(MainActivity.this, SongAlbum.class);
        System.out.println("debugging onAlbumTitleClickMore"+ albums_list.size());
        i.putExtra("album_song_one_item", albums_list.get(position));
        startActivity(i);
    }

    @Override
    public void onAlbumImageClickMore(int position) {
        Intent i = new Intent(MainActivity.this, SongAlbum.class);
        i.putExtra("album_song_one_item", albums_list.get(position));
        startActivity(i);
    }



    //@Override
    public void onSongTitleClicked(int position) {
        pos = position;
        Log.d(TAG,"Image Clicked on Position: "+position);
        Music current =onlineMusicsRecyclerviewAdapter.getItem(position);
        playMusic(current);
    }

    //@Override
    public void onSongImageClick(int position) {
        pos = position;
        Log.d(TAG,"Image Clicked on Position: "+position);
        Music current =onlineMusicsRecyclerviewAdapter.getItem(position);
        playMusic(current);
    }

    @Override
    public void onSearchViewShown() {

    }

    @Override
    public void onSearchViewClosed() {
        tvSearchAlbum.setText("");
        tvSearchLyrics.setText("");
        tvSearchSatsang.setText("");
        searchViewSatsangNameResult.clear();
        searchViewSatsangUrlResult.clear();

        ArrayList<Music> lstFoundSatsang = new ArrayList<>();
        onlineMusicsRecyclerview.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        //allSongsList.setAdapter(new SongsAdapterRecyclerView_Vertical(lstFoundSatsang, MainActivity.this));


    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText!=null && !newText.isEmpty() && newText.length()>=3)
        {
            Log.d("query", newText);


            ArrayList<Music> lstFoundSatsang = new ArrayList<>();
            int idx = 0;
            for(Music item:song_names)
            {
                if(item.getName().toLowerCase().contains(newText.toLowerCase()))
                {
                    lstFoundSatsang.add(item);
                    searchViewSatsangNameResult.add(item.getName());
                    searchViewSatsangUrlResult.add(song_urls.get(idx));
                }
                idx++;
            }
            if(lstFoundSatsang.size()==0)
                tvSearchSatsang.setText("No satsang found!");
            else
                tvSearchSatsang.setText("Ultimas Adicoes");
            onlineMusicsRecyclerview.addItemDecoration(itemDecorator);
            onlineMusicsRecyclerview.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            // allSongsList.setAdapter(new SongsAdapterRecyclerView_Vertical(lstFoundSatsang, MainActivity.this));



        }
        else
        {
            // return default
        }
        return true;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void retryPageLoad() {

    }
}
