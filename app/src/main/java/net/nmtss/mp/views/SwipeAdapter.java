package net.nmtss.mp.views;


import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import net.nmtss.mp.models.music.Music;

import java.util.ArrayList;

/**
 * Created by himanshu on 21/3/19.
 */

public class SwipeAdapter extends PagerAdapter {
    private final ArrayList<Music> musics;
    private int[] image_resources = {R.drawable.sample_0, R.drawable.sample_1, R.drawable.sample_2, R.drawable.sample_3, R.drawable.sample_5};
    private Context ctx;
    private LayoutInflater layoutInflater;

    public SwipeAdapter(Context ctx, ArrayList<Music> musics) {
        this.ctx = ctx;
        this.musics = musics;
    }

    @Override
    public int getCount() {
        return image_resources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==(LinearLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        ImageView imageView = item_view.findViewById(R.id.idImageSwipeLayout);
        TextView textView = item_view.findViewById(R.id.idItemCount);
        Picasso.get().load(musics.get(position).getFeaturedImg()).into(imageView);
        //imageView.setImageResource();
        textView.setText("Trending no : "+position);
        container.addView(item_view);
        return item_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);
    }
}
