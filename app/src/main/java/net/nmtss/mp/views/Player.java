package net.nmtss.mp.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Player extends Activity {
    Intent serviceIntent;
    Intent temp_serviceIntent = null;
    Intent temp_Intent = null;
    private ImageView buttonPlayStop;
    TextView tvSongLabel;
    int pos = 0;
    String playing_song_id = ""; //song id of playing song
    String prev_song_id = "";
    ImageView btPrev, btNext;
    Handler handler;
    Runnable runnable;
    ImageView imageView;

    // -- PUT THE NAME OF YOUR AUDIO FILE HERE...URL GOES IN THE SERVICE
    String strAudioLink = "";
    ProgressDialog progress;
    // --Seekbar variables --
    private SeekBar seekBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        if(!MusicManager.current_song_icon_url.matches(""))
        {
            Picasso.get().load(MusicManager.current_song_icon_url).into(imageView);
        }
        handler = new Handler();
        seekBar.setMax(MusicManager.player.getDuration());
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    MusicManager.player.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //playCycle should be called after setOnSeekBarChangeListener
        playCycle();

        buttonPlayStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicManager.isPlaying == true) {
                    MusicManager.player.pause();
                    MusicManager.isPlaying = false;
                    buttonPlayStop.setBackgroundResource(R.drawable.icon_play);
                } else {
                    MusicManager.isPlaying = true;
                    MusicManager.player.start();
                    buttonPlayStop.setBackgroundResource(R.drawable.icon_pause);
                }
            }
        });
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        final ArrayList<String> song_urls = bundle.getStringArrayList("song_urls");
        pos = bundle.getInt("song_pos", 0);
//        tvSongLabel.setText(bundle.getString("current_song_name"));
        tvSongLabel.setText(MusicManager.current_song_name);
        assert song_urls != null;
        String url = song_urls.get(pos);
        strAudioLink = Uri.parse(url).toString();
//        String[] temp = url.split("/");
//        final String song_name = temp[temp.length - 1];
//        tvSongLabel.setText(song_name);


        btNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CommonVariables.recent_pos!=-1)
                    pos = CommonVariables.recent_pos;
                pos = (pos + 1) % (song_urls.size());
                String[] temp = song_urls.get(pos).split("/");
                tvSongLabel.setText(temp[temp.length - 1]);
                //CommonVariables.recent_song_name.add(temp[temp.length - 1]);
                buttonPlayStop.setBackgroundResource(R.drawable.icon_pause);
                strAudioLink = song_urls.get(pos);
                //CommonVariables.recent_song_url.add(strAudioLink);
                // This is because next song is also from the same album => same song icon url
                // This will vary if next song is being played from Queue.. yet to implement
                //CommonVariables.recent_song_icon_url.add(MusicManager.current_song_icon_url);
                progress = new ProgressDialog(v.getContext());
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                MusicManager.SoundPlayer(v.getContext(), strAudioLink.replace(" ", "%20"), progress);
                handler.removeCallbacksAndMessages(null);
                handler = new Handler();
                seekBar.setMax(MusicManager.duration);
                seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser)
                            MusicManager.player.seekTo(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                //playCycle should be called after setOnSeekBarChangeListener
                playCycle();

            }
        });

        btPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CommonVariables.recent_pos!=-1)
                    pos = CommonVariables.recent_pos;
                pos = (song_urls.size() + pos - 1) % (song_urls.size());
                String[] temp = song_urls.get(pos).split("/");
                tvSongLabel.setText(temp[temp.length - 1]);
                //CommonVariables.recent_song_name.add(temp[temp.length - 1]);
                buttonPlayStop.setBackgroundResource(R.drawable.icon_pause);
                strAudioLink = song_urls.get(pos);
                //CommonVariables.recent_song_url.add(strAudioLink);
                // This is because next song is also from the same album => same song icon url
                // This will vary if next song is being played from Queue.. yet to implement
                //CommonVariables.recent_song_icon_url.add(MusicManager.current_song_icon_url);
                progress = new ProgressDialog(v.getContext());
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                MusicManager.SoundPlayer(v.getContext(), strAudioLink.replace(" ", "%20"), progress);
                handler.removeCallbacksAndMessages(null);
                handler = new Handler();
                seekBar.setMax(MusicManager.duration);
                seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser)
                            MusicManager.player.seekTo(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                //playCycle should be called after setOnSeekBarChangeListener
                playCycle();

            }
        });


    }

    public void playCycle() {
        seekBar.setProgress(MusicManager.player.getCurrentPosition());
        if (MusicManager.isPlaying) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    // --- Set up initial screen ---
    private void initViews() {
        buttonPlayStop = findViewById(R.id.idPlayPause);
        if (MusicManager.isPlaying == false)
            buttonPlayStop.setBackgroundResource(R.drawable.icon_play);
        else
            buttonPlayStop.setBackgroundResource(R.drawable.icon_pause);

        tvSongLabel = findViewById(R.id.idSongLabel);
        btNext = findViewById(R.id.idNext);
        btPrev = findViewById(R.id.idPrev);
        imageView = findViewById(R.id.idCoverImageForPlayer);

        // --Reference seekbar in main.xml
        seekBar = (SeekBar) findViewById(R.id.idSeekBar);
    }

}