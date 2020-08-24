package net.nmtss.mp.views;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import net.nmtss.mp.api.ApiUtils;
import net.nmtss.mp.api.MpEndpoint;
import net.nmtss.mp.models.album.Album;
import net.nmtss.mp.models.album.Bundled;
import net.nmtss.mp.models.music.Music;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by himanshu on 20/3/19.
 */
interface OnFetchSongInAlbumListener
{
    public void onUrlsFetched(ArrayList<String> urls);
    public void onUrlsError(String error);
}

public class SongAlbum extends AppCompatActivity implements OnFetchSongInAlbumListener, ItemAlbumSongAdapterRV_Vertical.OnSongClickListener {
    Album albumModel;
    ImageView imageView;
    ArrayList<Bundled> song_urls;
    ArrayList<Bundled> song_names ;
    String current_url, current_title;
    static int pos = 0;
    private static final int PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE = 20; //Arbitrary >= 0
    String TAG = "SongAlbum : download file ";
    ProgressDialog progress;
    String new_playlist_name = "";
    ImageView albumImage;
    ImageView mPlayerControl;
    TextView tvCurrentSong;
    int current_song_position=0;
    long downloadID;
    static boolean isFromMenu = false;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_album);
        imageView = findViewById(R.id.idAlbumCoverImageView);
        albumImage = findViewById(R.id.idSongAlbumImage);
        mPlayerControl = findViewById(R.id.idSongAlbumPlayPause);
        tvCurrentSong = findViewById(R.id.idSongAlbumName);



        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        tvCurrentSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SongAlbum.this, Player.class);
                i.putExtra("song_urls", (Serializable) albumModel.getBundled());
                i.putExtra("current_song_name", albumModel.getBundled().get(current_song_position));
                i.putExtra("song_pos", current_song_position);
                startActivity(i);
            }
        });

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
        //        //song_names=albumModel.getBundled().get(current_song_position).getMp3();
        albumModel = (Album) getIntent().getSerializableExtra("album_song_one_item");
        System.out.println("Debugging tge album Model");
        Picasso.get().load(albumModel.getFeaturedImg()).into(imageView);
        MusicManager.current_song_icon_url = albumModel.getFeaturedImg();
        Log.d("song url", albumModel.getFeaturedImg().substring(8));
        RecyclerView allLyricsList = findViewById(R.id.idAlbum);
        allLyricsList.setLayoutManager(new LinearLayoutManager(this));
        allLyricsList.setAdapter(new ItemAlbumSongAdapterRV_Vertical(albumModel, this));

    }












    @Override
    public void onSongTitleClick(int position) {
        current_song_position = position;
        Bundled song = albumModel.getBundled().get(position);
        mPlayerControl.setImageResource(R.drawable.icon_pause);
        tvCurrentSong.setText(Jsoup.parse(song.getTitle()).text().trim());
        //CommonVariables.recent_song_name.add(song_names.get(position));
        //CommonVariables.recent_song_url.add(song_urls.get(position));
        if (!MusicManager.current_song_icon_url.matches(""))
        {
            Picasso.get().load(MusicManager.current_song_icon_url).into(albumImage);
           // CommonVariables.recent_song_icon_url.add(MusicManager.current_song_icon_url);
        }
        else
        {
            albumImage.setBackgroundResource(R.drawable.icon_cover);
            //CommonVariables.recent_song_icon_url.add("R.drawable.icon_cover");
        }
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        //progress.show();
        MusicManager.SoundPlayer(this, song.getMp3(), progress);


    }

    @Override
    public void onSongImageClick(int position) {

    }


    @Override
    public void onAddToQueueClick(int position) {

        Bundled song = albumModel.getBundled().get(position);
        CommonVariables.queue_song_url.add(0, song.getMp3());
        CommonVariables.queue_song_icon_url.add(0, MusicManager.current_song_icon_url);
        CommonVariables.queue_song_name.add(0, Jsoup.parse(song.getTitle()).text().trim());
        Toast.makeText(this, "Added to the Queue", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOptionMenuClick(final int position, TextView textView) {
        pos = position;
        //Toast.makeText(this, "Option menu clicked!", Toast.LENGTH_SHORT).show();
        PopupMenu popupMenu = new PopupMenu(this, textView);
        popupMenu.inflate(R.menu.option_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Bundled song = albumModel.getBundled().get(pos);
                String name =Jsoup.parse(song.getTitle()).text().trim();
                switch (item.getItemId())
                {
                    case R.id.id_menu_share:
                        Toast.makeText(getApplicationContext(), "Share", Toast.LENGTH_SHORT).show();
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = "Share this with friends "+name;
                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Musica Promo");
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                        break;

                    case R.id.id_menu_download:
                        Toast.makeText(getApplicationContext(), "Download is clicked", Toast.LENGTH_SHORT).show();
                        //Runtime permission request required if Android permission >= Marshmallow
                        current_url = song.getMp3();
                        current_title = name;
                        Log.d("url of the song ", current_url);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            checkPermission(current_url, current_title);
                        else
//                          new DownloadFile().execute(current_url, current_title);
                            downloadFile(current_url, current_title);
                        break;

                    case R.id.id_menu_create_playlist:
                        final EditText playlist_name = new EditText(SongAlbum.this);

// Set the default text to a link of the Queen
                        playlist_name.setHint("Enter playlist name");

                        new AlertDialog.Builder(SongAlbum.this)
                                .setTitle("New Playlist")
                                .setView(playlist_name)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        DatabaseHelper myDb = new DatabaseHelper(getApplicationContext());
                                        myDb.insertPlaylistName(playlist_name.getText().toString());
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                                .show();
                        break;

                    case R.id.id_menu_add_to_playlist:
                        SongAlbum.isFromMenu = true;
                        Intent i = new Intent(SongAlbum.this, PlayLists.class);
                        i.putExtra("song_url", song.getMp3());
                        i.putExtra("song_icon_url", albumModel.getFeaturedImg());
                        i.putExtra("song_name", name);
                        startActivity(i);
                        break;

                    default:
                        return SongAlbum.super.onOptionsItemSelected(item);

                }
                return true;
            }
        });
        popupMenu.show();
    }


    private void checkPermission(String url, String title) {
        // Check if the permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED ) {
            // Permission is already available
            //new DownloadFile().execute(url, title);
            downloadFile(url, title);

        } else {
            // Permission is missing and must be requested.
            requestReadExternalStoragePermission();
        }
    }

    //download using Download Manager
    public void downloadFile(String uRl, String fileName) {
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/MyMusicPlayer");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(uRl.replace(" ", "%20"));
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle(fileName)
                .setDescription("Lyrics getting downloaded!")
                .setDestinationInExternalPublicDir("/MyMusicPlayer", fileName);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadID = mgr.enqueue(request);
        Toast.makeText(SongAlbum.this, "Download started!", Toast.LENGTH_SHORT).show();
    }

    private void requestReadExternalStoragePermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            ActivityCompat.requestPermissions(SongAlbum.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE);

        } else {
            // Request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE) {
            // Request for permission.
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
//                new DownloadFile().execute(current_url, current_title);
                downloadFile(current_url, current_title);
            } else {
                // Permission request was denied by user
                // Show a snackBar, exit program, close activity, etc.
            }
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
        else if(!tvCurrentSong.getText().toString().matches(""))
            mPlayerControl.setImageResource(R.drawable.icon_play);
    }

    @Override
    public void onUrlsFetched(ArrayList<String> urls) {

    }

    @Override
    public void onUrlsError(String error) {

    }


    BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(SongAlbum.this, "Download Completed", Toast.LENGTH_LONG).show();
            }
        }
    };


}
