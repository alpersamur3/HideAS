package com.a.s.hideAs;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Process.killProcess;
import static android.os.Process.myPid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.erikagtierrez.multiple_media_picker.Gallery;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;


public class MainActivity extends AppCompatActivity {

    private static final int OPEN_MEDIA_PICKER = 1;
    private static boolean doubleBackToExitPressedOnce = false;

    private final static String TAG = MainActivity.class.getSimpleName();
    final Context cont = this;
    private String MAIN_DIR = null;

    private final List<String> displayPaths = new ArrayList<>();
    public static int PERMISSION_REQUEST_CODE=101;
    private ArrayList<Integer> mSelected = new ArrayList<Integer>();
    private ArrayList<Integer> mSelected2 = new ArrayList<Integer>();
    private ArrayList<File> shareSelected = new ArrayList<File>();
    private ArrayList<String> selectedPaths = new ArrayList<String>();
    private ArrayList<View> selectedViews = new ArrayList<View>();
    //Creating object of AdView
    private AdView bannerAdView;
    private boolean adLoaded=false;


    FloatingActionMenu materialDesignFAM;
    //FloatingActionButton floatingActionButton1;
    FloatingActionButton floatingActionButton2;
    Button settings;
    Boolean add=false;
    Boolean permis=false;
    Boolean pickFolder=false;
    int tıklamaSayisi=0;
    String create="create";
    String ADS_ID=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        startedSF();
        MAIN_DIR = getResources().getString(R.string.main_dir);
        ADS_ID= getResources().getString(R.string.INTERSTITIAL_ADS_ID);
        //TODO Dark Mode Ekle
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        bannerAdView = (AdView) findViewById(R.id.bannerAdView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        LinearLayout imageTool=findViewById(R.id.imageTool);
        Button setting=findViewById(R.id.toolbarSettings);
        toolbar.setLogo(R.drawable.icon);
        toolbar.setTitle("  "+getResources().getString(R.string.app_name));

        //paylaşılmak üzere önbelleğe alınan resimleri temizler
        deleteDir(new File(getCacheDir()+"/images/"));


        if (!getFirst()){
            SFirst();
            startedS();
            Intent intent = new Intent(getBaseContext(), HelpActivity.class);
            startActivity(intent);
        }
        

        settings = findViewById(R.id.toolbarSettings);
        //ayarlar tuşuna basılınca settingsActivity i başlatır
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("ac", true);
                editor.commit();
                Intent intent= new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Floating menu button init
        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        //floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);



        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        loadBannerAd();


        /*
        Galeri görünümü oluşturulur
         */

        GridView gallery = (GridView) findViewById(R.id.galleryGridView);
        ImageAdapter adapter =new ImageAdapter(this);
        gallery.setAdapter(adapter);
        //bir resme basılı tutulursa titret ve seçimi başlat
        gallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!displayPaths.isEmpty()){
                    if (mSelected.size()==0) {
                        Vibrator v = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(100); // 5000 miliseconds = 5 seconds
                        setting.setVisibility(View.GONE);
                        imageTool.setVisibility(View.VISIBLE);
                        adapter.onItemSelect(adapterView, view, i, l);
                        bannerAdView.setVisibility(View.GONE);
                    }

            }
                return true;
            }
        });

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                //eğer seçim başladıysa tıklanan resmi seçilen resimler listesine ekle
                if (imageTool.getVisibility()==View.VISIBLE){
                    adapter.onItemSelect(arg0, arg1, position, arg3);
                }
                else {

                    if (tıklamaSayisi==4){
                        tıklamaSayisi=0;
                        loadAd(cont);
                    }
                    else if(tıklamaSayisi==0){
                        loadAd(cont);
                        tıklamaSayisi=tıklamaSayisi+1;
                    }
                    else {
                        tıklamaSayisi=tıklamaSayisi+1;
                    }

                    //tıklanan resmi GalleryV de göster
                    if (null != displayPaths && !displayPaths.isEmpty()) {
                        startedS();
                        Intent i = new Intent(MainActivity.this, GalleryV.class);
                        ArrayList<String> tumkonum = new ArrayList<>(displayPaths);

                        i.putStringArrayListExtra("paths", tumkonum);
                        i.putExtra("i", position);
                        i.putExtra("clicked", displayPaths.get(position));
                        startActivity(i);
                    }
                }
            }
        });




        /*
        menu item to create a new folder in app's directory with user input dialog
         */
 /*       floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onClick(View v) {

                isPermissionGranted();

                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(cont);
                View dial = layoutInflaterAndroid.inflate(R.layout.input_dialog_text, null);

                AlertDialog.Builder userInput = new AlertDialog.Builder(cont);
                userInput.setView(dial);
                Log.d(TAG, "onClick: INPUT DIALOG INFLATE");


                final EditText dialogEdit = (EditText) dial.findViewById(R.id.userInputDialogText);
                final TextView title = dial.findViewById(R.id.dialogTitleText);



                userInput.setCancelable(false).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void onClick(DialogInterface box, int id) {

                        String folderName = dialogEdit.getText().toString();
                        int good = folderName.length();


                        if(good != 0 && good <= 10 ) {
                            generateSecretFolder(folderName);

                            Toasty.success(getBaseContext(),"Folder Created with name " + folderName, Toast.LENGTH_SHORT, true).show();
                        }
                        else if(good == 0) {
                            box.cancel();
                            Toasty.warning(getBaseContext(),"Name can't be emtpy ", Toast.LENGTH_SHORT, true).show();
                        }
                        else if(good > 10) {
                            box.cancel();
                            Toasty.warning(getBaseContext(),"Name can't be more than 10 chars ", Toast.LENGTH_SHORT, true).show();
                        }


                    }
                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface box, int id) {
                                        box.cancel();
                                    }
                                });

                AlertDialog dialog = userInput.create();
                dialog.show();
                sharedSet(getDataDir() + getResources().getString(R.string.normal_file_folder) );
            }
        });
*/

        /*
        Resim Yükle Tuşu
         */
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(isPermissionGranted()){
                    openGallery();

                }

            }
        });

    }


    public void loadAd(Context context){
        final InterstitialAd[] mInterstitialAd = new InterstitialAd[1];

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, ADS_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd[0] = interstitialAd;
                super.onAdLoaded(interstitialAd);
                startedS();
                mInterstitialAd[0].show(MainActivity.this);
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        startedSF();
                        super.onAdDismissedFullScreenContent();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d(TAG, loadAdError.toString());
                mInterstitialAd[0] = null;
                super.onAdFailedToLoad(loadAdError);
            }
        });


    }


    //banner reklami yukler
    private void loadBannerAd()
    {
        // Creating  a Ad Request
        AdRequest adRequest = new AdRequest.Builder().build();

        // load Ad with the Request
        bannerAdView.loadAd(adRequest);
        bannerAdView.setVisibility(View.VISIBLE);


    }

    // resim seçiciyi çalıştırır
    public void openGallery() {
        startedS();
        add=true;
        normaleDon();
        Intent intent= new Intent(this, Gallery.class);
        intent.putExtra("title",R.string.file_picker_title);
        intent.putExtra("mode",1);
        intent.putExtra("maxSelection",R.integer.file_picker_max_selectable_file);
        startActivityForResult(intent,OPEN_MEDIA_PICKER);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==13){
            permis=false;
            startedSF();
        }

        if (requestCode == OPEN_MEDIA_PICKER) {
            // Make sure the request was successful


            if (resultCode == RESULT_OK && data != null) {

                ArrayList<String> selectionResult = data.getStringArrayListExtra("result"); // path of file
                materialDesignFAM.close(true);

                List<String> namesImg = getFileNames(selectionResult);
                generateSecretMain();

                for (int i = 0; i < selectionResult.size(); i++) {
                    moveFile(selectionResult.get(i), namesImg.get(i), MAIN_DIR, "");
                }

                reloadMain();

                // Logs for testing purposes
                if (!namesImg.isEmpty()) {
                    Log.d(TAG, "onActivityResult MEDIA PICK: ...................." + namesImg.get(0));
                    Log.d(TAG, "onActivityResult MEDIA PICK: ...................." + selectionResult.get(0));
                }
                Log.d(TAG, "onActivityResult: MEDIA PICKER finish ****************");

            } else {

            }
            startedSF();

        }
        //yani share intent sonucu
        else if (requestCode==10){
            startedSF();
        }
        //izin sonucu
        else if(requestCode==PERMISSION_REQUEST_CODE){
            if (isPermissionGranted()){
                Toasty.success(cont,getResources().getString(R.string.access_granted_text)).show();
                permis=false;
            }
            startedSF();
        }
        else if (requestCode==99){
            if (isPermissionGranted()){
                permis=false;
                Toasty.success(cont,getResources().getString(R.string.access_granted_text)).show();
            }
            startedSF();
        }
        else if (requestCode==9999){

            Uri uri = data.getData();
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));
            String path = getPathFromUri.getPath(this, docUri);
            final String[] secilenitem = new String[1];
            secilenitem[0]=path;
            String[] items = {getResources().getString(R.string.alert_selectable_item),path,getResources().getString(R.string.alert_selectable_item2)};

            LayoutInflater eulaInflater = LayoutInflater.from(MainActivity.this);
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
                    //.setMessage("Seçilen Dosyaları Geri Yüklemek(HideAS_reloaded klasörüne aktarılacak) İstediğinizden Emin Misiniz ?")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(getResources().getString(R.string.reload_text), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (!secilenitem[0].equals(getResources().getString(R.string.sdcard_error))) {
                                if (!secilenitem[0].equals(getResources().getString(R.string.reload_folder))) {
                                    String klasor = secilenitem[0].substring(secilenitem[0].lastIndexOf("/0/") + 3);
                                    if(alwaysUseThis.isChecked()) {
                                        setPath(klasor);
                                    }
                                    reloadFiles(klasor);
                                }
                                else{
                                    reloadFiles(secilenitem[0]);
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

    /*
    Gridview i yeniler
     */
    public void reloadMain() {

        GridView gallery = (GridView) findViewById(R.id.galleryGridView);

        gallery.setAdapter(new ImageAdapter(this));

    }


    // Gizlenen tüm resim ve videoları alır
    public void getSet() {

        File root = new File(getDataDir() + getResources().getString(R.string.normal_file_folder));


        ArrayList<String> myStrings = null;
        try {
            for (File f : root.listFiles()) {
                displayPaths.add(f.toString());

                /*String[] mas = f.toString().split("\\.");
               // if (mas[mas.length - 1].endsWith("hideASj") || mas[mas.length - 1].endsWith("hideASp") || mas[mas.length - 1].endsWith("mp4")) {//or other formats
                    //it is picture
                Toast.makeText(cont, f.toString(), Toast.LENGTH_SHORT).show();
                if (mas[mas.length - 1].endsWith("hideASp")) {
                    Log.d("foricif", f.toString());
                    String fileName = f.toString().substring(0, f.toString().lastIndexOf("hideASp"));
                    displayPaths.add(fileName + "png");
                    Log.d("foricipath", fileName + "png");
                } else if (mas[mas.length - 1].endsWith(getResources().getString(R.string.gif_file_extension))) {
                    Log.d("foricif", f.toString());
                    String fileName = f.toString().substring(0, f.toString().lastIndexOf(getResources().getString(R.string.gif_file_extension)));
                    displayPaths.add(fileName + "gif");
                    Log.d("foricipath", fileName + "gif");

                }
                else if (mas[mas.length - 1].endsWith("hideASj")){
                    Log.d("foricif", f.toString());
                    String fileName = f.toString().substring(0, f.toString().lastIndexOf("hideASj"));
                    Toast.makeText(cont, fileName, Toast.LENGTH_SHORT).show();

                    displayPaths.add(fileName + "jpg");
                    Log.d("foricipath", fileName + "jpg");
                }
                else if (mas[mas.length - 1].endsWith("getResources().getString(R.string.media_file_extension)")){
                    Log.d("foricif", f.toString());
                    String fileName = f.toString().substring(0, f.toString().lastIndexOf("getResources().getString(R.string.media_file_extension)"));
                    displayPaths.add(fileName + "mp4");
                    Log.d("foricipath", fileName + "mp4");

                }
                else {

                }*/

            }
        }
        catch(Exception e){
            isPermissionGranted();

        }


    }


    //dosya isimlerini çeker ve gönderir
    public static List<String> getFileNames(ArrayList<String> selectionResult) {
        List<String> namesImg = new ArrayList<>();
        for(int i = 0; i < selectionResult.size(); i++) {
            String name = selectionResult.get(i).trim();
            String newName;
            int a = selectionResult.get(i).lastIndexOf("/");
            int b = selectionResult.get(i).length();
            newName = name.substring(a+1,b);

            namesImg.add(newName);
        }
        return namesImg;
    }


    /*
    generates folder which is hidden
    */
    public void generateSecretFolder(String dirName) {
        //TODO cihaz içine depolama seçeneği eklenebilir
        try {

            File root = new File(getDataDir() + getResources().getString(R.string.normal_file_folder) + dirName);
            Log.d(TAG, "generateSecretFolder: " + getDataDir()+getResources().getString(R.string.normal_file_folder) + dirName);
            if (!root.exists()) {  // makes a new directory if there is none
                root.mkdirs();
            }
        } catch (Exception e) {
            Log.d(TAG, "generateFolder: Exception thrown");
        }
    }

    /*
    Gizli resimlerin duracağı ana klasörü oluşturur.
     */
    public void generateSecretMain() {
        try {

            File root = new File(getDataDir()+ MAIN_DIR);

            if (!root.exists()) {  // makes a new directory if there is none
                root.mkdirs();
                Log.d(TAG, "generateSecretFolder: main **************************");
            }
        } catch (Exception e) {
            Log.d(TAG, "generateFolder: Exception thrown");
        }
    }

    /*
    Şimdilik lazım değil ama klasör oluşturma için kullanılabilir
     */
    public void generateFolder(String dirName) {
        try {

            File root = new File(getDataDir()+ getResources().getString(R.string.normal_file_folder) + dirName);
            Log.d(TAG, "generateSecretFolder: " + getDataDir() + getResources().getString(R.string.normal_file_folder) + dirName);
            if (!root.exists()) {  // makes a new directory if there is none
                root.mkdirs();
            }
        } catch (Exception e) {

            Log.d(TAG, "generateFolder: Exception thrown");
        }
    }


    /*
    Dosya taşıma,geri yükleme,paylaşmak için cache ye atma gibi pek çok işlevi vardır
     */
    public void moveFile(String location, String name,String target,String dat) {

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
                    //şuanki uzantı (getResources().getString(R.string.image_file_extension))
                    String currentExtension=name.substring(0,name.lastIndexOf("."));
                    //orjinal uzantı
                    String extension = currentExtension.substring(currentExtension.lastIndexOf("."));
                    //dosya uzantısı değiştirildi
                    File file=changeExtension(new File(Environment.getExternalStorageDirectory()  + target , name),extension);
                    //resmin adı alındı
                    String edit_path=file.getName().toString();
                    //dosya yolu alındı
                    String path=file.getPath();
                    //uzantı string ten çıkarıldı
                    String soedited=edit_path.substring(0,edit_path.lastIndexOf("."));
                    //fazladan uzantı string ten çıkarıldı
                    String sosoedited=soedited.substring(0,soedited.lastIndexOf("."));
                    String edited = sosoedited.substring(0,sosoedited.lastIndexOf("."));
                    destination =new File(getCacheDir()  +target+edited+extension);
                }


            }
            //eğer geri yüklemeyse
            else if (Objects.equals(dat, "ext")){
                if (name.endsWith(getResources().getString(R.string.media_file_extension))) {
                    destination =changeExtension(new File(Environment.getExternalStorageDirectory() + target , name),".mp4");
                }
                else if (name.endsWith(getResources().getString(R.string.gif_file_extension))){
                    destination =changeExtension(new File(Environment.getExternalStorageDirectory()  + target , name),".gif");
                }
                else{
                    //şuanki uzantı (getResources().getString(R.string.image_file_extension))
                    String currentExtension=name.substring(0,name.lastIndexOf("."));
                    //orjinal uzantı
                    String extension = currentExtension.substring(currentExtension.lastIndexOf("."));
                    //resmin uzantısı değiştirildi
                    String edit_path=changeExtension(new File(Environment.getExternalStorageDirectory()  + target , name),extension).toString();
                    //uzantı string ten çıkarıldı
                    String soedited=edit_path.substring(0,edit_path.lastIndexOf("."));
                    //fazladan uzantı string ten çıkarıldı
                    String sosoedited=soedited.substring(0,soedited.lastIndexOf("."));
                    String edited = sosoedited.substring(0,sosoedited.lastIndexOf("."));
                    destination =new File(edited+extension);
                }
            }
        }
        //normal kullanım dosyaları cihazdan data klasörüne atar
        else{
            if (name.endsWith(".mp4")) {
                destination =changeExtension(new File(getDataDir() + target , name),getResources().getString(R.string.media_file_extension));
            }
            else if (name.endsWith(".gif")){
                destination =changeExtension(new File(getDataDir() + target , name),getResources().getString(R.string.gif_file_extension));
            }
            else{
                destination =changeExtension(new File(getDataDir() + target , name),getResources().getString(R.string.image_file_extension));
            }
           //destination = new File(getDataDir() + getResources().getString(R.string.normal_file_folder), name);
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
                    Log.d(TAG, "moveFile: File moved ********************************" + source + destination);
                    if(!dat.equals("data_in")) {
                        source.delete();  // delete file from gallery and send broadcast to update gallery
                        cont.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(location))));
                        Toasty.success(cont, getResources().getString(R.string.success_loading_files), Toast.LENGTH_SHORT, true).show();
                    }
                    else{
                        shareSelected.add(destination);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "moveFile: Exception called ********************************" + e.getMessage());
            }


    }

    public static File changeExtension(File f, String newExtension) {
        int i = f.getName().lastIndexOf('.');
        String original=f.getName().substring(i);
        String name = f.getName().substring(0,i);
        return new File(f.getParent(), name +original+ newExtension);
    }

    //giriş çıkıslarda şifre sormamayı sağlayan prefleri ayarlar
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
    //sürekli kullanılacak geri yükleme dizinini ayarlar
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

    public Boolean getFirst(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        Boolean result=prefs.getBoolean("first",false);
        return result;
    }

    public void SFirst(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("first", true);
        editor.commit();
    }
    public void startedS(){
        SharedPreferences prefs = getSharedPreferences("activityC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("ac", true);
        editor.commit();
    }

    @Override
    //uygulama arka plana atılınca ekranı gizle ve kilitle
    protected void onPause() {
        startedSF();
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
        if (!sendedGet()) {
            //resim ekleme yeri acilip kapatildiysa sifre sor.
            if (add) {
                Toasty.warning(cont, getResources().getString(R.string.toast_security_after_file_upload), 4).show();
                startedS();
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                intent.putExtra("hey", true);
                startActivityForResult(intent, 13);
                add = false;
            }
            else if(pickFolder){
                pickFolder=false;

            }
            //izin alma ekranı değilse şifre sor(yani normal kullanım)
            else if (!permis) {

                if (!startedGet()) {
                    startedS();
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    intent.putExtra("hey", true);
                    startActivityForResult(intent, 13);
                }
                if (mSelected2.size()==0) {
                    reloadMain();
                }
            }
        }
        else{
            sendedSF();
        }
        super.onStart();
    }

    /*
        exit on 2 back button actions
         */
    @Override
    public void onBackPressed() {
        if (mSelected2.size()!=0) {
            normaleDon();
        }
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            killProcess(myPid());
        }

        doubleBackToExitPressedOnce = true;
        Toasty.normal(MainActivity.this, getResources().getString(R.string.toast_double_back_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    //izin iste
    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startedS();
                permis=true;
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                permis=true;
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startedS();
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }
        } else {
            //below android 11
            startedS();
            permis=true;
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 99);
        }
    }

    // izinleri kontrol et
    public  boolean isPermissionGranted() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()){
                return true;
            }
            else {
                Toasty.warning(cont,getResources().getString(R.string.toast_request_denied_message)).show();
                requestPermission();
                return false;
            }

        }
        else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted *********");
                return true;
            } else {
                Toasty.warning(cont,getResources().getString(R.string.toast_request_denied_message)).show();
                Log.v("TAG", "Permission is not present * * * * ** *");
                requestPermission();
                return false;
            }
        }


    }

    //dosyaları sil
    public Boolean delFiles(){
        boolean deleted=false;
        ArrayList<String> secilen = new ArrayList<>();
        for (int i=0; 0!=mSelected.size(); i++){
            secilen.add(displayPaths.get(mSelected.get(0)));
            mSelected.remove(0);
        }
        List<String> resimAdiList=getFileNames(secilen);
        //resimin adini cevirip string olarak aldık
        for (int i=0; resimAdiList.size()!=0; i++){
            String resimAdi = resimAdiList.get(0);
            resimAdiList.remove(0);
            String path=secilen.get(i).substring(0,secilen.get(i).lastIndexOf("/"));

            File file=new File(path,resimAdi);
            deleted = file.delete();

            LinearLayout imageTool = findViewById(R.id.imageTool);
            Button setting = findViewById(R.id.toolbarSettings);
            imageTool.setVisibility(View.GONE);
            setting.setVisibility(View.VISIBLE);

        }
        reloadMain();
        return deleted;

    }
    //dosyaları geri yükle HideAS_reloaded konumuna
    public Boolean reloadFiles(String target){
        ArrayList<String> secilen = new ArrayList<>();
        for (int i=0; 0!=mSelected.size(); i++){
            secilen.add(displayPaths.get(mSelected.get(0)));
            mSelected.remove(0);
        }
        List<String> resimAdiList=getFileNames(secilen);
        //resimin adini cevirip string olarak aldık
        for (int i=0; resimAdiList.size()!=0; i++){
            String resimAdi = resimAdiList.get(0);
            resimAdiList.remove(0);
            moveFile(secilen.get(i), resimAdi, "/"+target+"/", "ext");
            LinearLayout imageTool = findViewById(R.id.imageTool);
            Button setting = findViewById(R.id.toolbarSettings);
            imageTool.setVisibility(View.GONE);
            setting.setVisibility(View.VISIBLE);

        }
        reloadMain();
        return true;
    }
    //paylaşma intent ini başlat
    public void share(ArrayList<File> path){
        ArrayList<Uri> contentUri = new ArrayList<Uri>();
        if (contentUri != null) {
            Intent shareIntent = new Intent();
            if (path.size()==1){
                Uri urij = FileProvider.getUriForFile(MainActivity.this, "com.a.s.hideAs.fileprovider", path.get(0));
                contentUri.add(urij);
                Uri uri = contentUri.get(0);
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setClipData(ClipData.newRawUri("", uri));
            }
            else{
                for (File paths : path /* List of the files you want to send */) {
                    Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.a.s.hideAs.fileprovider", paths);
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
    //dosyaları paylaşmak için hazırla(cache ye gönder)
    public Boolean shareFiles(){
        ArrayList<String> secilen = new ArrayList<>();
        for (int i=0; 0!=mSelected.size(); i++){
            secilen.add(displayPaths.get(mSelected.get(0)));
            mSelected.remove(0);
        }
        List<String> resimAdiList=getFileNames(secilen);
        //paylaşma listesini temizledik
        shareSelected.clear();
        //resimin adini cevirip string olarak aldık
        for (int i=0; resimAdiList.size()!=0; i++){
            String resimAdi = resimAdiList.get(0);
            resimAdiList.remove(0);
            moveFile(secilen.get(i), resimAdi, "/images/", "data_in");
            LinearLayout imageTool = findViewById(R.id.imageTool);
            Button setting = findViewById(R.id.toolbarSettings);
            imageTool.setVisibility(View.GONE);
            setting.setVisibility(View.VISIBLE);
        }
        share(shareSelected);
        normaleDon();
        return true;
    }
    //dizini sil
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
    //seçilenleri kaldır

    public void normaleDon() {
        try {
            int i = 0;
            LinearLayout imageTool = findViewById(R.id.imageTool);
            imageTool.setVisibility(View.GONE);
            Button setting = findViewById(R.id.toolbarSettings);
            setting.setVisibility(View.VISIBLE);
            bannerAdView.setVisibility(View.VISIBLE);

            for (View v : selectedViews) {
                ImageView selectedButton = v.findViewById(R.id.mediaButton);
                RelativeLayout back = (RelativeLayout) v.findViewById(R.id.normalView);
                back.setBackgroundResource(R.color.white);
                back.setPadding(0, 0, 0, 0);
                if (displayPaths.get(mSelected2.get(i)).endsWith(getResources().getString(R.string.media_file_extension))) {
                    selectedButton.setImageResource(R.drawable.media_512x512);
                } else {
                    selectedButton.setVisibility(View.GONE);
                }
                i = i + 1;
            }
            mSelected.clear();
            mSelected2.clear();
            selectedPaths.clear();
            selectedViews.clear();
        }
        catch (Exception e){
            Log.d("expect",e.getMessage().toString());
        }

    }
    //gridview i ayarla
    private class ImageAdapter extends BaseAdapter {

        private final Activity context;
        private View view;


        public ImageAdapter(Activity localContext) {
            context = localContext;
            displayPaths.clear();
            getSet();

            Log.d(TAG, "ImageAdapter: " + displayPaths.size());


        }




        public void onItemSelect(AdapterView<?> parent, View v, int pos, long id) {
            Integer position = new Integer(pos);

            RelativeLayout back = (RelativeLayout) v.findViewById(R.id.normalView);
            Button reload = (Button) findViewById(R.id.reload);
            Button delete = (Button) findViewById(R.id.delete);
            Button share = (Button) findViewById(R.id.share);
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareFiles();
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context, R.style.CustomAlertDialog)
                            .setTitle(getResources().getString(R.string.alert_delete_title))
                            .setMessage(getResources().getString(R.string.alert_delete_message))

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(getResources().getString(R.string.alert_delete_positive), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                    if (delFiles()) {
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
            reload.setOnClickListener(new View.OnClickListener() {
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
                    AlertDialog.Builder builder=new AlertDialog.Builder(context, R.style.CustomAlertDialog)
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
                            //.setMessage("Seçilen Dosyaları Geri Yüklemek(HideAS_reloaded klasörüne aktarılacak) İstediğinizden Emin Misiniz ?")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(getResources().getString(R.string.reload_text), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (finalItems.length==2) {
                                        reloadFiles(getResources().getString(R.string.reload_folder));
                                    }
                                    else{
                                        reloadFiles(getSPath());
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
            ImageView selectedButton = v.findViewById(R.id.mediaButton);
            if (mSelected.contains(position)) {
                mSelected.remove(position);
                mSelected2.remove(position);

                // update view (v) state here
                // eg: remove highlight
            } else {
                mSelected.add(position);
                mSelected2.add(position);
                selectedViews.add(v);
                selectedPaths.add(displayPaths.get(position));

            }
            // update view (v) state here
            if (mSelected.contains(position)) {
                back.setPadding(15, 15, 15, 15);
                selectedButton.setImageResource(R.drawable.tick);
                back.setBackgroundResource(R.color.funnyGreen);
                selectedButton.setVisibility(View.VISIBLE);
            } else {
                if (mSelected.size() == 0) {
                    LinearLayout imageTool = findViewById(R.id.imageTool);
                    Button setting = findViewById(R.id.toolbarSettings);
                    imageTool.setVisibility(View.GONE);
                    setting.setVisibility(View.VISIBLE);
                    bannerAdView.setVisibility(View.VISIBLE);
                }
                if (displayPaths.get(position).endsWith(getResources().getString(R.string.media_file_extension))) {
                    selectedButton.setImageResource(R.drawable.media_512x512);
                } else {
                    selectedButton.setVisibility(View.GONE);
                }
                back.setBackgroundResource(R.color.white);
                selectedViews.remove(v);
                selectedPaths.remove(displayPaths.get(position));
                back.setPadding(0, 0, 0, 0);
            }

        }

        public int getCount() {
            return displayPaths.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ResourceAsColor")
        public View getView(final int position, View convertView,
                            ViewGroup parent) {

            ImageView picturesView;
            RelativeLayout back;
            TextView tv = (TextView) findViewById(R.id.selectedCount);

            ImageView selectedButton;
            View hangisi = null;
            View v = convertView;

            if (v == null) {
                // getting reference to the main layout and initializing
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.griditem, parent, false);
            }
            picturesView = (ImageView) v.findViewById(R.id.picture);
            back = (RelativeLayout) v.findViewById(R.id.normalView);
            selectedButton = v.findViewById(R.id.mediaButton);
            hangisi = v;
            tv.setText(String.valueOf(mSelected.size()));

            if (displayPaths.get(position).endsWith(getResources().getString(R.string.media_file_extension))) {

                Glide.with(context).load(displayPaths.get(position))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(picturesView);
                selectedButton.setVisibility(View.VISIBLE);

            } else if (displayPaths.get(position).endsWith(getResources().getString(R.string.image_file_extension))) {

                Glide.with(context).load(displayPaths.get(position)).into(picturesView);
            } else if (displayPaths.get(position).endsWith(getResources().getString(R.string.gif_file_extension))) {
                Glide.with(context).load(displayPaths.get(position)).asGif().crossFade().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(picturesView);
            }


            return hangisi;
        }

    }
}
