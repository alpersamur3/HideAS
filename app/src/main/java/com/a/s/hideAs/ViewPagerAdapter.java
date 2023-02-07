package com.a.s.hideAs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {

    private ArrayList<String> paths=new ArrayList<>();
    private Context context;


    public ViewPagerAdapter(ArrayList<String> paths, Context context) {
        this.paths = paths;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater. from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        LinearLayout altbar=(LinearLayout) getActivity(context).findViewById(R.id.altbar);
        LinearLayout ustbar=(LinearLayout) getActivity(context).findViewById(R.id.ustbar);

        altbar.bringToFront();
        ustbar.bringToFront();

        if (paths.get(position).contains(".hideASm")){
            holder.mediaButton.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.mediaImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(paths.get(position))
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Drawable d = new BitmapDrawable(context.getResources(), resource);
                            holder.mediaImage.setImageDrawable(d);
                        }
                    });
        }
        else if ((paths.get(position).contains(".hideASi"))){
            Glide.with(context).load(paths.get(position)).into(holder.imageView);
            holder.mediaButton.setVisibility(View.GONE);
            holder.mediaImage.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);

        }
        else if ((paths.get(position).contains(".hideASg"))){
            Glide.with(context).load(paths.get(position)).asGif().crossFade().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.imageView);
            holder.mediaButton.setVisibility(View.GONE);
            holder.mediaImage.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
        }


        holder.mediaImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryV v = new GalleryV();
                //resme tıklandığında tam ekran moduna geç

                if (ustbar.getAlpha() != 0.0f) {
                    v.hideSystemUI(getActivity(context));
                    ustbar.animate().alpha(0.0f);
                    altbar.animate().translationY(altbar.getHeight());
                    altbar.bringToFront();
                    ustbar.bringToFront();

                } else {
                    v.showSystemUI(getActivity(context));
                    altbar.animate().translationY(0);
                    ustbar.animate().alpha(1.0f);
                    altbar.bringToFront();
                    ustbar.bringToFront();
                }


            }
        });



        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryV v = new GalleryV();

                if (ustbar.getAlpha() != 0.0f) {
                    v.hideSystemUI(getActivity(context));
                    ustbar.animate().alpha(0.0f);
                    altbar.animate().translationY(altbar.getHeight());
                    altbar.bringToFront();
                    ustbar.bringToFront();

                } else {
                    v.showSystemUI(getActivity(context));
                    altbar.animate().translationY(0);
                    ustbar.animate().alpha(1.0f);
                    altbar.bringToFront();
                    ustbar.bringToFront();
                }


            }
        });
        holder.mediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startedS();
                Intent i = new Intent(context, videoViewActivity.class);
                i.putExtra("i",position);
                i.putExtra("clicked",paths.get(position));
                GalleryV v=new GalleryV();
                context.startActivity(i);
            }
        });



    }



    public void startedS(){
        SharedPreferences prefs = context.getSharedPreferences("activityC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("ac", true);
        editor.commit();
    }


    @Override
    public int getItemCount() {
        return paths.size();
    }
    public static Activity getActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) return (Activity) context;
        if (context instanceof ContextWrapper) return getActivity(((ContextWrapper)context).getBaseContext());
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        PhotoView imageView;
        ImageView mediaButton;
        ImageView mediaImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=(PhotoView) itemView.findViewById(R.id.imageViewMain);
            mediaButton=(ImageView) itemView.findViewById(R.id.mediaButton);
            mediaImage=(ImageView) itemView.findViewById(R.id.mediaImage);

        }
    }
}