package net.nmtss.mp.views;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class QueueSongs extends AppCompatActivity implements QueueAdapterRecyclerView_Vertical.OnQueueSongClickListener
{
    DividerItemDecoration itemDecorator;
    ProgressDialog progress;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_songs);
        itemDecorator = new DividerItemDecoration(QueueSongs.this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(QueueSongs.this, R.drawable.divider));

        progress = new ProgressDialog(this);
        RecyclerView allSatsangList = findViewById(R.id.idQueueSongs);
        allSatsangList.addItemDecoration(itemDecorator);
        allSatsangList.setLayoutManager(new LinearLayoutManager(this));
        allSatsangList.setAdapter(new QueueAdapterRecyclerView_Vertical(this));
    }

    @Override
    public void onQueueSongTitleClick(int position) {
        CommonVariables.queue_pos = position;
        MusicManager.current_song_icon_url = CommonVariables.queue_song_icon_url.get(position);
        Log.d("queue song ", MusicManager.current_song_icon_url);
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        MusicManager.SoundPlayer(this, CommonVariables.queue_song_url.get(position), progress);

    }

    @Override
    public void onQueueSongImageClick(int position) {
        CommonVariables.queue_pos = position;
        MusicManager.current_song_icon_url = CommonVariables.queue_song_icon_url.get(position);
        Log.d("queue song ", MusicManager.current_song_icon_url);
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        MusicManager.SoundPlayer(this, CommonVariables.queue_song_url.get(position), progress);

    }
}

