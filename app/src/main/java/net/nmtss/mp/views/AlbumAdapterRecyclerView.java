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


public class AlbumAdapterRecyclerView extends RecyclerView.Adapter<AlbumAdapterRecyclerView.AlbumViewHolder> {

    private ArrayList<Album> albums;
    private OnAlbumClickListener mOnAlbumClickListener;

    public AlbumAdapterRecyclerView(ArrayList<Album> albumModelArrayList, OnAlbumClickListener onAlbumClickListener)
    {
        this.albums = albumModelArrayList;
        this.mOnAlbumClickListener = onAlbumClickListener;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.album_item_recycler_view, parent, false);
        return new AlbumViewHolder(view, mOnAlbumClickListener);
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
        OnAlbumClickListener onAlbumClickListener;
        public AlbumViewHolder(View itemView, OnAlbumClickListener onAlbumClickListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.idImgAlbumRV);
            textView = itemView.findViewById(R.id.idDescAlbumRV);
            this.onAlbumClickListener = onAlbumClickListener;
            imageView.setOnClickListener(this);
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v==imageView)
                onAlbumClickListener.onAlbumImageClick(getAdapterPosition());
            else if(v==textView)
                onAlbumClickListener.onAlbumTitleClick(getAdapterPosition());
        }
    }

    interface OnAlbumClickListener
    {
        void onAlbumTitleClick(int position);
        void onAlbumImageClick(int position);
    }
}
