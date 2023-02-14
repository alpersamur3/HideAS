package com.a.s.hideAs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class HelpActivity extends AppCompatActivity {

    TextView info1;
    TextView info2;
    TextView danger;
    Toolbar toolbar;
    String back="no";
    Boolean create=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        startedSF();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);

        info1 = findViewById(R.id.first_info);
        info2 = findViewById(R.id.second_info);
        danger=findViewById(R.id.danger);

        info1.setText(getResources().getString(R.string.app_description));
        danger.setText(getResources().getString(R.string.app_danger));
        info2.setText(getResources().getString(R.string.app_usage));


    }

    public Boolean startedGet(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        Boolean result=prefs.getBoolean("ac",false);
        return result;
    }

    public void startedSF(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("ac", false);
        editor.commit();
    }
    public void startedS(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("ac", true);
        editor.commit();
    }


    @Override
    protected void onPause() {
        if (!back.equals("back")){
            startedSF();
        }
        super.onPause();
/*        if (!startedGet()) {
            startedS();
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            intent.putExtra("hey", true);
            startActivityForResult(intent, 13);
            super.onPause();
        }
        else{
            super.onPause();
        }*/

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
                    startedS();
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    intent.putExtra("hey", true);
                    startActivityForResult(intent, 13);
                    super.onPause();
                }
                else {
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
    public void onBackPressed() {
        back="back";
        startedS();
        super.onBackPressed();
    }

}
