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

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;


import com.squareup.picasso.Picasso;

import net.nmtss.mp.models.music.Music;

import org.jsoup.Jsoup;


/**
 * Created by himanshu on 26/2/19.
 */

public class SongsAdapterRecyclerView_Vertical extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    OnMoreSongItemClickListner mOnMoreSongItemClickedListener;
    private ArrayList<Music> data;
    // View Types
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

    SongsAdapterRecyclerView_Vertical(Context context, OnMoreSongItemClickListner mOnMoreSongItemClickedListener){
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) context;
        musics = new ArrayList<>();
        this.mOnMoreSongItemClickedListener = mOnMoreSongItemClickedListener;
    }

    public List<Music> getMusics() {
        return musics;
    }

    public void setMusics(List<Music> musics) {
        this.musics = musics;
    }




    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG,"on create causing issues " +musics.size());
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.more_music_list_layout, parent, false);
                viewHolder = new MusicVH(viewItem, mOnMoreSongItemClickedListener);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
            case HERO:
                View viewHero = inflater.inflate(R.layout.no_music_list_layout, parent, false);
                viewHolder = new NoMusicVH(viewHero, mOnMoreSongItemClickedListener);
                break;
        }
        return viewHolder;





    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Music music = musics.get(position);
        //holder.textView.setText(music.getName());
        String imgUrl ="";
        if(music.getFeaturedImg()!=null){
            imgUrl=music.getFeaturedImg();

        }
        System.out.println(imgUrl);

        switch (getItemViewType(position)) {

            case HERO:
                final NoMusicVH heroVh = (NoMusicVH) holder;

                heroVh.mMusicTitle.setText(music.getName());
                heroVh.mMusicDesc.setText(music.getContent());
                heroVh.mYear.setText(music.getDate());
                Picasso.get().load(imgUrl).into(heroVh.mPosterImg);

                break;

            case ITEM:
                final MusicVH musicVH = (MusicVH) holder;

                musicVH.mMusicTitle.setText(music.getName());
                musicVH.mMusicDesc.setText(music.getContent());
                musicVH.mYear.setText(music.getDate());
                Picasso.get().load(imgUrl).into(musicVH.mPosterImg);

                break;

            case LOADING:
                LoadingVH loadingVH = (LoadingVH) holder;

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
        return musics==null?0:musics.size();
    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HERO;
        } else {
            return (position == musics.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
        }
    }




    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView textView;
        OnMoreSongItemClickListner onMoreSongItemClickListner;
        public SongViewHolder(View itemView, OnMoreSongItemClickListner onMoreSongItemClickListner) {
            super(itemView);
            imageView = itemView.findViewById(R.id.idImgSongRV_Vertical);
            textView = itemView.findViewById(R.id.idDescSongRV_Vertical);
            this.onMoreSongItemClickListner = onMoreSongItemClickListner;

            imageView.setOnClickListener(this);
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v==imageView)
                onMoreSongItemClickListner.onSongImageClick(getAdapterPosition());
            else if(v==textView)
                onMoreSongItemClickListner.onSongTitleClicked(getAdapterPosition());
        }
    }

    public interface OnMoreSongItemClickListner
    {
        void onSongTitleClicked(int position);
        void onSongImageClick(int position);
        void onSongDescriptionClicked(int position);
        void onSongYearClicked(int position);

    }
/*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    public void add(Music r) {
        Music x = r;
        if(x.getContent()!=null)
            x.setContent(Jsoup.parse(r.getContent()).text().trim());
        musics.add(x);
        notifyItemInserted(musics.size() - 1);

    }

    public void addAll(List<Music> musics) {
        for (Music music : musics) {

            add(music);
        }
    }

    public void remove(Music r) {
        int position = musics.indexOf(r);
        if (position > -1) {
            musics.remove(position);
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

        int position = musics.size() - 1;
        Music result = getItem(position);

        if (result != null) {
            musics.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Music getItem(int position) {
        return musics.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(musics.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }


    private class NoMusicVH extends RecyclerView.ViewHolder implements View.OnClickListener  {
        OnMoreSongItemClickListner onMoreSongItemClickListner;
        private TextView mMusicTitle;
        private TextView mMusicDesc;
        private TextView mYear; // displays "year | language"
        private ImageView mPosterImg;

        public NoMusicVH(View itemView, OnMoreSongItemClickListner onMoreSongItemClickListner) {
            super(itemView);

            mMusicTitle = itemView.findViewById(R.id.song_title);
            mMusicDesc = itemView.findViewById(R.id.song_desc);
            mYear = itemView.findViewById(R.id.song_year);
            mPosterImg = itemView.findViewById(R.id.song_poster);
            this.onMoreSongItemClickListner = onMoreSongItemClickListner;

            mMusicDesc.setOnClickListener(this);
            mMusicTitle.setOnClickListener(this);
            mYear.setOnClickListener(this);
            mPosterImg.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v==mPosterImg)
                onMoreSongItemClickListner.onSongImageClick(getAdapterPosition());
            else if(v== mMusicTitle)
                onMoreSongItemClickListner.onSongTitleClicked(getAdapterPosition());
            else if(v== mMusicDesc)
                onMoreSongItemClickListner.onSongDescriptionClicked(getAdapterPosition());
            else if(v== mYear)
                onMoreSongItemClickListner.onSongYearClicked(getAdapterPosition());
        }
    }

    private class MusicVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final View mProgress;
        OnMoreSongItemClickListner onMoreSongItemClickListner;
        private TextView mMusicTitle;
        private TextView mMusicDesc;
        private TextView mYear; // displays "year | language"
        private ImageView mPosterImg;

        public MusicVH(View itemView, OnMoreSongItemClickListner onMoreSongItemClickListner) {
            super(itemView);
            mMusicTitle = itemView.findViewById(R.id.music_title);
            mMusicDesc = itemView.findViewById(R.id.music_desc);
            mYear = itemView.findViewById(R.id.music_year);
            mPosterImg = itemView.findViewById(R.id.music_poster);

            this.onMoreSongItemClickListner = onMoreSongItemClickListner;
            mProgress = itemView.findViewById(R.id.music_progress);

            mMusicDesc.setOnClickListener(this);
            mMusicTitle.setOnClickListener(this);
            mYear.setOnClickListener(this);
            mPosterImg.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if(v==mPosterImg)
                onMoreSongItemClickListner.onSongImageClick(getAdapterPosition());
            else if(v== mMusicTitle)
                onMoreSongItemClickListner.onSongTitleClicked(getAdapterPosition());
            else if(v== mMusicDesc)
                onMoreSongItemClickListner.onSongDescriptionClicked(getAdapterPosition());
            else if(v== mYear)
                onMoreSongItemClickListner.onSongYearClicked(getAdapterPosition());
        }
    }

    private class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        public LoadingVH(View itemView)  {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout);

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

}

