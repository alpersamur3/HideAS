package com.a.s.hideAs;

import static com.a.s.hideAs.Hashing.getSha1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private TextView welcome;
    private final static String TAG = LoginActivity.class.getSimpleName();
    public static final String PASSWORD_PREF = "PassPref";
    public static final String PASSWORD_STORE = "PinSalt";
    private String check;
    private Boolean clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clicked = extras.getBoolean("hey");
            }
            else{
                clicked=false;
            }
        }
        catch(Exception e){
            clicked=false;
        }


        mPinLockView = findViewById(R.id.pin_lock_view);
        mIndicatorDots = findViewById(R.id.indicator_dots);

        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(mPinLockListener);

        /*
        Shuffle if settings slider is ON
         */
        SharedPreferences shuffle = getSharedPreferences(SettingsActivity.SETTINGS_PREF, 0);

        boolean isOn = shuffle.getBoolean(SettingsActivity.SHUFFLE_STORE, false);
        if(isOn) {
            mPinLockView.enableLayoutShuffling();
        }


        mPinLockView.setPinLength(5);
        mPinLockView.setTextColor(ContextCompat.getColor(this, R.color.white));

        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
    }

    @Override
    protected void onStart() { // Invoke PIN Code creation if the user is new
        super.onStart();
        check = getPin();
        if(check.isEmpty()) {
            Toasty.warning(LoginActivity.this, getResources().getString(R.string.toast_warning_create_pin), Toast.LENGTH_LONG, true).show();
        }
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
    public void send(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("send", true);
        editor.commit();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!clicked) {
            Intent f = new Intent(Intent.ACTION_MAIN);
            f.addCategory(Intent.CATEGORY_HOME);
            f.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(f);
        }
        else{
            finishAffinity();
        }
    }

    /*
        If pincode input by the user that is equal to SHA1 hashed pincode in the device's memory, let the user pass, if not - give other ccommands
        such as incorrect pin or new user pin
     */
    private PinLockListener mPinLockListener = new PinLockListener() {

            @Override
            public void onComplete (String pin){
            Log.d(TAG, "Pin complete: " + pin);
            check = getPin();
            String salt = getSha1(pin);
            if (check.isEmpty()) {
                setPin(pin);
                send();
                Toasty.success(LoginActivity.this, getResources().getString(R.string.toast_success_created_pin) + pin, Toast.LENGTH_LONG, true).show();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
            else if (check.equals(salt)){
                send();
                if (clicked) {
                    startedS();
                    finish();
                }
                else {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
            else {
                Toasty.error(LoginActivity.this, getResources().getString(R.string.toast_wrong_password), Toast.LENGTH_SHORT, true).show();
            }
            }

            @Override
            public void onEmpty () {
            Log.d(TAG, "Pin empty");

            }

            @Override
            public void onPinChange ( int pinLength, String intermediatePin){
            Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void setPin(String pin) {
        SharedPreferences passFile = getSharedPreferences(PASSWORD_PREF,0);  // mode private if 0
        SharedPreferences.Editor myeditor = passFile.edit();
        String salt = getSha1(pin);
        myeditor.putString("PinSalt", salt);
        myeditor.apply();
    }

    public String getPin() {
        SharedPreferences passFile = getSharedPreferences(PASSWORD_PREF,0);
        return passFile.getString(PASSWORD_STORE, "");
    }
}
