package net.nmtss.mp.views;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import net.nmtss.mp.models.album.Album;

import java.util.ArrayList;

/**
 * Created by himanshu on 26/2/19.
 */

public class AlbumAdapterRecyclerView_Vertical extends RecyclerView.Adapter<AlbumAdapterRecyclerView_Vertical.AlbumViewHolder> {

    private ArrayList<Album> albums;
    private OnAlbumClickListenerMore mOnAlbumClickListenerMore;
    public AlbumAdapterRecyclerView_Vertical(ArrayList<Album> albums, OnAlbumClickListenerMore onAlbumClickListenerMore)
    {
        this.albums = albums;
        this.mOnAlbumClickListenerMore = onAlbumClickListenerMore;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.album_item_recycler_view_vertical, parent, false);
        return new AlbumViewHolder(view, mOnAlbumClickListenerMore);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        Album album = albums.get(position);

        holder.textView.setText(album.getName());
        String imgUrl ="";
        if(album.getFeaturedImg()!=null){
            imgUrl=album.getFeaturedImg();
            Picasso.get().load(imgUrl).into(holder.imageView);

        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView textView;
        OnAlbumClickListenerMore onAlbumClickListenerMore;
        public AlbumViewHolder(View itemView, OnAlbumClickListenerMore onAlbumClickListenerMore) {
            super(itemView);
            imageView = itemView.findViewById(R.id.idImgAlbumRV_Vertical);
            textView = itemView.findViewById(R.id.idDescAlbumRV_Vertical);
            this.onAlbumClickListenerMore = onAlbumClickListenerMore;
            imageView.setOnClickListener(this);
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v==imageView)
                onAlbumClickListenerMore.onAlbumImageClickMore(getAdapterPosition());
            else if(v==textView)
                onAlbumClickListenerMore.onAlbumTitleClickMore(getAdapterPosition());
        }
    }
    interface OnAlbumClickListenerMore
    {
        void onAlbumTitleClickMore(int position);
        void onAlbumImageClickMore(int position);
    }
}
