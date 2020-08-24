package net.nmtss.mp.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import net.nmtss.mp.models.music.Music;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by himanshu on 26/2/19.
 */

public class SongsAdapterRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Music> data;

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final int HERO = 2;
    private static final String TAG = "SongAdapter";

    //private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w200";

    private List<Music> musics;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;

    //SongsAdapterRecyclerView_Vertical.OnMoreSongItemClickListner mOnMoreSongItemClickedListener;
    private OnSongClickListener onSongClickListener;

    public SongsAdapterRecyclerView(Context context, OnSongClickListener onSongClickListener) {
        //this.data = data;

        this.context = context;
        this.mCallback = (PaginationAdapterCallback) context;
        data = new ArrayList<>();
        this.onSongClickListener = onSongClickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // View view = layoutInflater.inflate(R.layout.songs_item_recycler_view, parent, false);
        //return new SongViewHolder(view, onSongClickListener);

        Log.d(TAG, "on create causing issues " + data.size());
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.main_music_list_layout, parent, false);
                viewHolder = new SongViewHolder(viewItem, onSongClickListener);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.main_item_progress, parent, false);
                viewHolder = new LoadingMusicVH(viewLoading, onSongClickListener);
                break;
            case HERO:
                View viewHero = inflater.inflate(R.layout.main_music_list_layout, parent, false);
                viewHolder = new NoMainMusicVH(viewHero, onSongClickListener);
                break;
        }
        return viewHolder;


    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Music music = data.get(position);
        //holder.textView.setText(music.getName());
        String  imgUrl=music.getFeaturedImg();

        Log.d(TAG, "Loading " +position);

        switch (getItemViewType(position)) {

            case HERO:
                final SongsAdapterRecyclerView.NoMainMusicVH heroVh = (SongsAdapterRecyclerView.NoMainMusicVH) holder;

                heroVh.mMusicTitle.setText(music.getName());
                //heroVh.mMusicDesc.setText(music.getContent());
                //heroVh.mYear.setText(music.getDate());
                Picasso.get().load(imgUrl).into(heroVh.mPosterImg);

                break;

            case ITEM:
                final SongsAdapterRecyclerView.SongViewHolder musicVH = (SongsAdapterRecyclerView.SongViewHolder) holder;

                musicVH.textView.setText(music.getName());
                //musicVH.mMusicDesc.setText(music.getContent());
                //musicVH.mYear.setText(music.getDate());
                Picasso.get().load(imgUrl).into(musicVH.imageView);

                break;

            case LOADING:
                SongsAdapterRecyclerView.LoadingMusicVH loadingVH = (SongsAdapterRecyclerView.LoadingMusicVH) holder;

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
        }


    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView textView;
        OnSongClickListener onSongClickListener;

        public SongViewHolder(View itemView, OnSongClickListener onSongClickListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.idImgSongRV);
            textView = itemView.findViewById(R.id.idDescSongRV);
            this.onSongClickListener = onSongClickListener;
            imageView.setOnClickListener(this);
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == textView)
                onSongClickListener.onSongClick(getAdapterPosition());
            else
                onSongClickListener.onImageClick(getAdapterPosition());
        }
    }

    public interface OnSongClickListener {
        void onSongClick(int position);

        void onImageClick(int position);
    }


    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    public void add(Music r) {
        Music x = r;
        if (x.getContent() != null)
            x.setContent(Jsoup.parse(r.getContent()).text().trim());
        data.add(x);
        notifyItemInserted(data.size() - 1);

    }

    public void addAll(List<Music> musics) {
        for (Music music : musics) {

            add(music);
        }
    }

    public void remove(Music r) {
        int position = data.indexOf(r);
        if (position > -1) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Music());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = data.size() - 1;
        Music result = getItem(position);

        if (result != null) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Music getItem(int position) {
        return data.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(data.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }


    private class LoadingMusicVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;
        OnSongClickListener onSongClickListener;

        public LoadingMusicVH(View itemView, OnSongClickListener onSongClickListener) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout);
            this.onSongClickListener = onSongClickListener;
            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }

    private class NoMainMusicVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        SongsAdapterRecyclerView.OnSongClickListener onMoreSongItemClickListener;
        private TextView mMusicTitle;
        private TextView mMusicDesc;
        private TextView mYear; // displays "year | language"
        private ImageView mPosterImg;

        public NoMainMusicVH(View itemView, SongsAdapterRecyclerView.OnSongClickListener onSongClickListner) {
            super(itemView);

            mMusicTitle = itemView.findViewById(R.id.song_title);
            mMusicDesc = itemView.findViewById(R.id.song_desc);
            mYear = itemView.findViewById(R.id.song_year);
            mPosterImg = itemView.findViewById(R.id.song_poster);
            this.onMoreSongItemClickListener = onSongClickListner;

            mMusicDesc.setOnClickListener(this);
            mMusicTitle.setOnClickListener(this);
            mYear.setOnClickListener(this);
            mPosterImg.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == mPosterImg)
                onMoreSongItemClickListener.onImageClick(getAdapterPosition());
            else if (v == mMusicTitle)
                onMoreSongItemClickListener.onSongClick(getAdapterPosition());
            else if (v == mMusicDesc)
                onMoreSongItemClickListener.onSongClick(getAdapterPosition());
            else if (v == mYear)
                onMoreSongItemClickListener.onSongClick(getAdapterPosition());
        }
    }
}
