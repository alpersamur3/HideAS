package com.a.s.hideAs;

import static com.a.s.hideAs.MainActivity.changeExtension;
import static com.a.s.hideAs.MainActivity.getFileNames;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class GalleryV extends AppCompatActivity {

    String clicked;
    ArrayList<String> pathsList;
    Integer selectedCount;
    ViewPagerAdapter mViewPagerAdapter;
    final Context cont = this;
    Boolean create=true;
    String back="no";

    ViewPager2 viewPager;
    Boolean pickFolder=false;
    private String MAIN_DIR = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galleryv);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        MAIN_DIR = getResources().getString(R.string.main_dir);
        viewPager = (ViewPager2) findViewById(R.id.viewPager);
        Button backButton=(Button)findViewById(R.id.backButton);
        Button delButton=(Button) findViewById(R.id.delete);
        Button shareButton=(Button) findViewById(R.id.share);
        Button reloadButton=(Button) findViewById(R.id.reload);

        startedSF();
        //gonderilen veriler aliniyor
        if (clicked==null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clicked = extras.getString("clicked");
                pathsList = extras.getStringArrayList("paths");
                selectedCount = extras.getInt("i");
            }
        }
        //yeni adapter olusturulup viewpager2 ayarlan??yor.
        mViewPagerAdapter = new ViewPagerAdapter(pathsList, this);
        viewPager.setAdapter(mViewPagerAdapter);
        viewPager.setCurrentItem(selectedCount);


        getWindow().getDecorView()
                .setOnSystemUiVisibilityChangeListener(visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        //full screen'e gelip gelmedi??i kontrol edilebilir
                    }
                });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(GalleryV.this, R.style.CustomAlertDialog)
                        .setTitle(getResources().getString(R.string.alert_delete_title))
                        .setMessage(getResources().getString(R.string.alert_delete_message))

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(getResources().getString(R.string.alert_delete_positive), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                ArrayList<String> sil=new ArrayList<String >();
                                sil.add(pathsList.get(viewPager.getCurrentItem()));
                                if (delFiles(sil,viewPager.getCurrentItem())) {
                                    Toasty.success(cont, getResources().getString(R.string.toast_success_delete)).show();
                                } else {
                                    Toasty.error(cont, getResources().getString(R.string.toast_error_delete)).show();
                                }
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(getResources().getString(R.string.alert_negative_button), null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> shared=new ArrayList<String>();
                shared.add(pathsList.get(viewPager.getCurrentItem()));
                shareFiles(shared);
            }
        });
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] secilenitem = {getResources().getString(R.string.alert_selectable_item)};
                String[] items = {getResources().getString(R.string.alert_selectable_item), getResources().getString(R.string.alert_selectable_item2)};
                if (!getSPath().equals("")) {
                    items = new String[]{getSPath()+" "+getResources().getString(R.string.alert_selectable_item3), getResources().getString(R.string.alert_selectable_item), getResources().getString(R.string.alert_selectable_item2)};
                    secilenitem[0]=getSPath();
                }
                int leng=items.length;
                int checkedItem = 0;
                String[] finalItems = items;
                AlertDialog.Builder builder=new AlertDialog.Builder(GalleryV.this, R.style.CustomAlertDialog)
                        .setTitle(getResources().getString(R.string.reload_alert_title))
                        .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (leng==3){
                                    if (which==0){
                                        secilenitem[0]=getSPath();
                                    }
                                    else if(which==1){
                                        secilenitem[0]=getResources().getString(R.string.reload_folder);
                                    }
                                    else if(which==2){
                                        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                        i.addCategory(Intent.CATEGORY_DEFAULT);
                                        pickFolder=true;
                                        startActivityForResult(Intent.createChooser(i, getResources().getString(R.string.folder_picker_title)), 9999);
                                        dialog.dismiss();
                                    }
                                }
                                else if (leng==2){
                                    if (which==0){
                                        secilenitem[0]=getResources().getString(R.string.reload_folder);
                                    }
                                    else if(which==1){
                                        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                        i.addCategory(Intent.CATEGORY_DEFAULT);
                                        startActivityForResult(Intent.createChooser(i, getResources().getString(R.string.folder_picker_title)), 9999);
                                        dialog.dismiss();
                                    }
                                }
                            }
                        })
                        //.setMessage("Se??ilen Dosyalar?? Geri Y??klemek(HideAS_reloaded klas??r??ne aktar??lacak) ??stedi??inizden Emin Misiniz ?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(getResources().getString(R.string.reload_text), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ArrayList<String> reloaded=new ArrayList<String >();
                                reloaded.add(pathsList.get(viewPager.getCurrentItem()));
                                if (finalItems.length==2) {
                                    reloadFiles(reloaded, viewPager.getCurrentItem(),getResources().getString(R.string.reload_folder));
                                }
                                else{
                                    reloadFiles(reloaded, viewPager.getCurrentItem(),secilenitem[0]);
                                }
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(getResources().getString(R.string.alert_negative_button), null)
                        .setIcon(android.R.drawable.ic_dialog_alert);
                AlertDialog a=builder.create();
                a.show();
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==9999){

            Uri uri = data.getData();
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));
            String path = getPathFromUri.getPath(this, docUri);
            final String[] secilenitem = new String[1];
            secilenitem[0]=path;
            String[] items = {getResources().getString(R.string.alert_selectable_item),path,getResources().getString(R.string.alert_selectable_item2)};

            LayoutInflater eulaInflater = LayoutInflater.from(GalleryV.this);
            View eulaLayout = eulaInflater.inflate(R.layout.dialog_checkbox, null);
            CheckBox alwaysUseThis = (CheckBox)eulaLayout.findViewById(R.id.checkBox);
            alwaysUseThis.setChecked(true);

            int checkedItem = 1;
            AlertDialog.Builder builder=new AlertDialog.Builder(cont, R.style.CustomAlertDialog)
                    .setTitle(getResources().getString(R.string.reload_alert_title))
                    .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which==0){
                                secilenitem[0] =getResources().getString(R.string.reload_folder);
                            }
                            else if (which==1){
                                secilenitem[0]=items[which];

                            }
                            else if (which == 2) {
                                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                pickFolder=true;
                                startActivityForResult(Intent.createChooser(i, getResources().getString(R.string.folder_picker_title)), 9999);
                                dialog.dismiss();
                            }
                        }
                    })
                    //.setMessage("Se??ilen Dosyalar?? Geri Y??klemek(HideAS_reloaded klas??r??ne aktar??lacak) ??stedi??inizden Emin Misiniz ?")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(getResources().getString(R.string.reload_text), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayList<String> reloaded=new ArrayList<String >();
                            reloaded.add(pathsList.get(viewPager.getCurrentItem()));
                            if (!secilenitem[0].equals(getResources().getString(R.string.sdcard_error))) {
                                if (!secilenitem[0].equals(getResources().getString(R.string.reload_folder))) {
                                    String klasor = secilenitem[0].substring(secilenitem[0].lastIndexOf("/0/") + 3);
                                    if(alwaysUseThis.isChecked()) {
                                        setPath(klasor);
                                    }
                                    reloadFiles(reloaded, viewPager.getCurrentItem(),klasor);
                                }
                                else{
                                    reloadFiles(reloaded,viewPager.getCurrentItem(),secilenitem[0]);
                                }
                            }
                            else{
                                Toasty.error(cont,getResources().getString(R.string.error_loading_files),Toast.LENGTH_LONG).show();
                            }

                        }
                    })
                    .setView(eulaLayout)
                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(getResources().getString(R.string.alert_negative_button), null)
                    .setIcon(android.R.drawable.ic_dialog_alert);
            AlertDialog a=builder.create();
            a.show();
        }
    }

    public String getSPath(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        String result=prefs.getString("dir","");
        return result;
    }
    public void setPath(String dir){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("dir",dir);
        editor.commit();
    }

    public void reloadView(int position){
        mViewPagerAdapter = new ViewPagerAdapter(pathsList, this);
        viewPager.setAdapter(mViewPagerAdapter);
        try {
            viewPager.setCurrentItem(position);
        }
        catch (Exception e){
            viewPager.setCurrentItem(position-1);
        }
    }

    //dosyalar?? sil
    public Boolean delFiles(ArrayList<String> location,int position){
        boolean deleted=false;

        List<String> resimAdiList=getFileNames(location);
        //resimin adini cevirip string olarak ald??k
        String resimAdi = resimAdiList.get(0);
        resimAdiList.remove(0);
        String path=location.get(0).substring(0,location.get(0).lastIndexOf("/"));
        File file=new File(path,resimAdi);
        deleted = file.delete();

        Toasty.success(cont,getResources().getString(R.string.toast_success_delete)).show();
        pathsList.remove(position);
        reloadView(position);
        return deleted;

    }
    //dosyalar?? geri y??kle HideAS_reloaded konumuna
    public Boolean reloadFiles(ArrayList<String> location,int position,String target){

        List<String> resimAdiList=getFileNames(location);
        //resimin adini cevirip string olarak ald??k
        String resimAdi = resimAdiList.get(0);
        resimAdiList.remove(0);
        moveFile(location.get(0), resimAdi, "/"+target+"/", "ext");

        pathsList.remove(position);
        reloadView(position);
        return true;
    }
    //payla??ma intent ini ba??lat
    public void share(ArrayList<File> path){
        ArrayList<Uri> contentUri = new ArrayList<Uri>();
        if (contentUri != null) {
            Intent shareIntent = new Intent();
            if (path.size()==1){
                Uri urij = FileProvider.getUriForFile(GalleryV.this, "com.a.s.hideAs.fileprovider", path.get(0));
                contentUri.add(urij);
                Uri uri = contentUri.get(0);
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setClipData(ClipData.newRawUri("", uri));
            }
            else{
                for (File paths : path /* List of the files you want to send */) {
                    Uri uri = FileProvider.getUriForFile(GalleryV.this, "com.a.s.hideAs.fileprovider", paths);
                    contentUri.add(uri);
                }
                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,contentUri);
                ClipData clipData = ClipData.newRawUri("", contentUri.get(0));

                for (int i = 1; i < contentUri.size(); i++) {
                    Uri uri = contentUri.get(i);
                    clipData.addItem(new ClipData.Item(uri));
                }
                shareIntent.setClipData(clipData);
            }
            shareIntent.setType("*/*");

            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            startedS();
            startActivityForResult(Intent.createChooser(shareIntent, getResources().getString(R.string.share_files_title)),10);

        }



    }


    //dosyalar?? payla??mak i??in haz??rla(cache ye g??nder)
    public Boolean shareFiles(ArrayList<String> location) {
        List<String> resimAdiList = getFileNames(location);
        //resimin adini cevirip string olarak ald??k
        String resimAdi = resimAdiList.get(0);
        resimAdiList.remove(0);
        ArrayList<File> locate = new ArrayList<File>();
        locate.add(moveFile(location.get(0), resimAdi, "/images/", "data_in"));
        share(locate);
        return true;
    }



    /*
Dosya ta????ma,geri y??kleme,payla??mak i??in cache ye atma gibi pek ??ok i??levi vard??r
 */
    public File moveFile(String location, String name, String target, String dat) {

        File source= new File(location);
        Boolean err=false;
        File destination =null;
        if (target!=MAIN_DIR) {
            //share mode
            if (dat.equals("data_in")) {
                if (name.endsWith(getResources().getString(R.string.media_file_extension))) {
                    destination =changeExtension(new File(getCacheDir() + target , name),".mp4");
                }
                else if (name.endsWith(getResources().getString(R.string.gif_file_extension))){
                    destination =changeExtension(new File(getCacheDir()  + target , name),".gif");
                }
                else{
                    //??uanki uzant?? (hideASi)
                    String currentExtension=name.substring(0,name.lastIndexOf("."));
                    //orjinal uzant??
                    String extension = currentExtension.substring(currentExtension.lastIndexOf("."));
                    //dosya uzant??s?? de??i??tirildi
                    File file=changeExtension(new File(Environment.getExternalStorageDirectory()  + target , name),extension);
                    //resmin ad?? al??nd??
                    String edit_path=file.getName().toString();
                    //dosya yolu al??nd??
                    String path=file.getPath();
                    //uzant?? string ten ????kar??ld??
                    String soedited=edit_path.substring(0,edit_path.lastIndexOf("."));
                    //fazladan uzant?? string ten ????kar??ld??
                    String sosoedited=soedited.substring(0,soedited.lastIndexOf("."));
                    String edited = sosoedited.substring(0,sosoedited.lastIndexOf("."));
                    destination =new File(getCacheDir()  +target+edited+extension);
                }


            }
            //e??er geri y??klemeyse
            else if (Objects.equals(dat, "ext")){
                if (name.endsWith(getResources().getString(R.string.media_file_extension))) {
                    destination =changeExtension(new File(Environment.getExternalStorageDirectory() + target , name),".mp4");
                }
                else if (name.endsWith(getResources().getString(R.string.gif_file_extension))){
                    destination =changeExtension(new File(Environment.getExternalStorageDirectory()  + target , name),".gif");
                }
                else{
                    //??uanki uzant?? (hideASi)
                    String currentExtension=name.substring(0,name.lastIndexOf("."));
                    //orjinal uzant??
                    String extension = currentExtension.substring(currentExtension.lastIndexOf("."));
                    //resmin uzant??s?? de??i??tirildi
                    String edit_path=changeExtension(new File(Environment.getExternalStorageDirectory()  + target , name),extension).toString();
                    //uzant?? string ten ????kar??ld??
                    String soedited=edit_path.substring(0,edit_path.lastIndexOf("."));
                    //fazladan uzant?? string ten ????kar??ld??
                    String sosoedited=soedited.substring(0,soedited.lastIndexOf("."));
                    String edited = sosoedited.substring(0,sosoedited.lastIndexOf("."));
                    destination =new File(edited+extension);
                }
            }
        }
        try {
            File dr = new File(destination.toString().substring(0,destination.toString().lastIndexOf("/")+1));
            if (!dr.exists()) {
                dr.mkdir();
            }

            if (source.exists()) {
                FileChannel src = new FileInputStream(source).getChannel();
                FileChannel dst = new FileOutputStream(destination).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Log.d("(GalleryV)", "moveFile: File moved ********************************" + source + destination);
                if(!dat.equals("data_in")) {
                    source.delete();  // delete file from gallery and send broadcast to update gallery
                    cont.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(location))));
                    Toasty.success(cont, getResources().getString(R.string.single_succes_loading_files), Toast.LENGTH_SHORT, true).show();
                }


            }
        } catch (Exception e) {
            Log.d("(GalleryV)", "moveFile: Exception called ********************************" + e.getMessage());
        }
        return destination;
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
        if(pickFolder){
            pickFolder=false;

        }
        else {
            startedSF();
        }
        super.onStart();
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


    @Override
    protected void onPause() {
        if (!back.equals("back")) {
            startedSF();
        }

        super.onPause();



    }

    @Override
    public void onBackPressed() {
        startedS();
        back="back";
        super.onBackPressed();
    }


    @SuppressLint("InlinedApi")
    //tum gorunumleri fullscreen moduna sokar.
    private static final int UI_OPTIONS = View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;


    //ust ve alt bar?? gizler
    public void hideSystemUI(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);
    }
    //ust ve alt bar?? tekrar g??r??n??r yapar.
    public void showSystemUI(Activity activity){
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

}