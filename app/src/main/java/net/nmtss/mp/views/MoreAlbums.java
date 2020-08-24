package net.nmtss.mp.views;

import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import net.nmtss.mp.models.album.Album;

import java.util.ArrayList;

/**
 * Created by himanshu on 26/2/19.
 */

public class MoreAlbums extends AppCompatActivity implements AlbumAdapterRecyclerView_Vertical.OnAlbumClickListenerMore{
    ArrayList<Album> albumModelArrayList;
    DividerItemDecoration itemDecorator;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_more);
        System.out.println("Debug More");
        itemDecorator = new DividerItemDecoration(MoreAlbums.this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(MoreAlbums.this, R.drawable.divider));

        albumModelArrayList = (ArrayList<Album>) getIntent().getSerializableExtra("more_albums");

        RecyclerView allAlbumsList = findViewById(R.id.idAllAlbums);
        allAlbumsList.addItemDecoration(itemDecorator);
        allAlbumsList.setLayoutManager(new LinearLayoutManager(this));
        allAlbumsList.setAdapter(new AlbumAdapterRecyclerView_Vertical(albumModelArrayList, this));
    }

    @Override
    public void onAlbumTitleClickMore(int position) {
        //Toast.makeText(this, "title position : "+position, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(MoreAlbums.this, SongAlbum.class);
        i.putExtra("album_song_one_item", albumModelArrayList.get(position));
        startActivity(i);
    }

    @Override
    public void onAlbumImageClickMore(int position) {
        //Toast.makeText(this, "image position : "+position, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(MoreAlbums.this, SongAlbum.class);
        i.putExtra("album_song_one_item", albumModelArrayList.get(position));
        startActivity(i);
    }
}
