package com.a.s.hideAs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import xyz.doikki.videocontroller.StandardVideoController;
import xyz.doikki.videoplayer.player.VideoView;


public class videoViewActivity extends AppCompatActivity {

    VideoView videoView;
    String clickedVideoPath;
    Boolean create=true;
    String back="no";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoview);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);

        videoView=(VideoView) findViewById(R.id.player);
        startedSF();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            clickedVideoPath = extras.getString("clicked");
        }

        videoView.setUrl(String.valueOf(Uri.fromFile(new File(clickedVideoPath))));
        StandardVideoController controller = new StandardVideoController(this);
        controller.addDefaultControlComponent(getResources().getString(R.string.app_name), false);
        videoView.setVideoController(controller);
        videoView.startFullScreen();

        getWindow().getDecorView()
                .setOnSystemUiVisibilityChangeListener(visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        //full screen'e gelip gelmediği kontrol edilebilir
                        videoView.startFullScreen();
                    }
                });
        videoView.start();


    }

    //tum gorunumleri fullscreen moduna sokar.
    private static final int UI_OPTIONS = View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;


    @Override
    protected void onPause() {
        videoView.pause();
        if(!back.equals("back")){
            startedSF();
        }
        super.onPause();
    }


    public Boolean sendedGet(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        Boolean result=prefs.getBoolean("send",false);
        return result;
    }

    public void sendedSF() {
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("send", false);
        editor.commit();

    }

    @Override
    protected void onStart() {
        if (!sendedGet()){
            if (!create){
                if (!startedGet()) {
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    intent.putExtra("hey", true);
                    startActivityForResult(intent, 13);
                }
                else{
                    startedSF();
                }
            }
            else{
                create=false;
            }
        }
        else{
            sendedSF();
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.release();
    }




    @Override
    public void onBackPressed() {
        startedS();
        back="back";
        finish();
        super.onBackPressed();
    }
    public Boolean startedGet(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        Boolean result=prefs.getBoolean("ac",false);
        return result;
    }

    public void startedS(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("ac", true);
        editor.commit();
    }
    public void startedSF(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("ac", false);
        editor.commit();
    }

}
