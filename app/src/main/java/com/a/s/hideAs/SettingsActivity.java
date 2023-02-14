package com.a.s.hideAs;

import static com.a.s.hideAs.Hashing.getSha1;
import static com.a.s.hideAs.LoginActivity.PASSWORD_PREF;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import es.dmoral.toasty.Toasty;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "Settings";
    public static final String SETTINGS_PREF = "SettingsPref";
    public static final String SHUFFLE_STORE = "Shuffle";

    final Context mCont =  this;
    String back="no";

    Button help;
    Button feedback;
    Button pincode;
    Switch shuffle;
    Boolean feed=false;
    Boolean create=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        startedSF();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.getNavigationIcon().setColorFilter(Color.rgb(255,255,255), PorterDuff.Mode.SRC_IN); // White arrow
        toolbar.setTitle(getResources().getString(R.string.settings_title));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

         help = findViewById(R.id.help);
         feedback = findViewById(R.id.feedback);
         pincode = findViewById(R.id.pincode);
         shuffle = findViewById(R.id.on_off_switch);

         /*
         // at the start set slider on/off by looking at shared prefs
          */
        SharedPreferences settings = getSharedPreferences(SETTINGS_PREF, 0);
        boolean slider = settings.getBoolean(SHUFFLE_STORE, false);
        shuffle.setChecked(slider);

         help.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startedS();
                 Intent intent = new Intent(getBaseContext(), HelpActivity.class);
                 startActivity(intent);

             }
         });

         feedback.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + getResources().getString(R.string.feedback_mail)));
                 intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback_subject));
                 intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.feedback_text));
                 startActivity(intent);
                 feed=true;
             }
         });

         /*
         Changing pincode functionality
          */

         pincode.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mCont);
                 View dial = layoutInflaterAndroid.inflate(R.layout.input_dialog, null);

                 AlertDialog.Builder userInput = new AlertDialog.Builder(mCont,R.style.CustomAlertDialog);
                 userInput.setView(dial);
                 Log.d(TAG, "onClick: INPUT DIALOG INFLATE");


                  final EditText dialogEdit = (EditText) dial.findViewById(R.id.userInputDialog);
                  final TextView title = dial.findViewById(R.id.dialogTitle);


                    // Making input error prone, user can't use more or less than 5 numbers
                 userInput.setCancelable(false).setPositiveButton(getResources().getString(R.string.password_alert_save_button), new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface box, int id) {

                                 String newPin = dialogEdit.getText().toString();
                                 int good = newPin.length();
                                 Log.d(TAG, "onClick: PIN ENTRY *******************************");

                                 if(good == 5) {
                                     SharedPreferences passFile = getSharedPreferences(PASSWORD_PREF,0);  // mode private if 0
                                     SharedPreferences.Editor myeditor = passFile.edit();
                                     String salt = getSha1(newPin);

                                     Log.d(TAG, "onClick: PIN SALT *******************************" + salt);

                                     myeditor.putString("PinSalt", salt);
                                     myeditor.apply();

                                     Toasty.success(getBaseContext(),getResources().getString(R.string.toast_pin_changed) + newPin, Toast.LENGTH_SHORT, true).show();
                                 }
                                 else {
                                     box.cancel();
                                     Toasty.warning(getBaseContext(),getResources().getString(R.string.toast_pin_changed_error_5letter), Toast.LENGTH_SHORT, true).show();
                                 }

                             }
                         })
                         .setNegativeButton(getResources().getString(R.string.alert_negative_button),
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface box, int id) {
                                         box.cancel();
                                     }
                                 });

                 AlertDialog dialog = userInput.create();
                 dialog.show();
             }
         });

         /*
         Shuffle pin-code functionality
          */

         shuffle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 Log.d(TAG, "onCheckedChanged: " + isChecked);

                 SharedPreferences settingsFile = getSharedPreferences(SETTINGS_PREF,0);  // mode private if 0
                 SharedPreferences.Editor myEditor = settingsFile.edit();

                 if(isChecked) {

                     myEditor.putBoolean(SHUFFLE_STORE, isChecked);
                     myEditor.apply();
                 }
                 else {
                     myEditor.putBoolean(SHUFFLE_STORE, isChecked);
                     myEditor.apply();
                 }
             }
         });



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
        if (!sendedGet()) {
            if (!create) {
                if (!startedGet()) {
                    if (feed) {
                        feed = false;
                        startedS();
                        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                        intent.putExtra("hey", true);
                        startActivityForResult(intent, 13);
                        startedS();
                    } else {
                        startedS();
                        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                        intent.putExtra("hey", true);
                        startActivityForResult(intent, 13);
                    }
                } else {
                    startedSF();
                }
            } else {
                create = false;
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

    @Override
    protected void onPause() {
        if (!back.equals("back")){
            startedSF();
        }
        super.onPause();
/*        if (feed) {
            super.onPause();
        }
        else {
            if (!startedGet()) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                intent.putExtra("hey", true);
                startActivityForResult(intent, 13);
                super.onPause();


            } else {
                super.onPause();

            }
        }*/



    }


}
