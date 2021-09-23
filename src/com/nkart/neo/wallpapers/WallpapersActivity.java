package com.nkart.neo.wallpapers;

import android.Manifest;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.launcher3.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.nkart.neo.extra.AnimatorUtils;
import com.nkart.neo.extra.Extra;
import com.nkart.neo.extra.ParallaxPagerTransformer;
import com.nkart.neo.utils.Config;
import com.nkart.neo.wallpapers.db.DBHelper;
import com.nkart.neo.wallpapers.fragments.FragmentImageInfo;
import com.nkart.neo.wallpapers.model.LegacyCompatFileProvider;
import com.nkart.neo.wallpapers.model.PictureModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.ogaclejapan.arclayout.ArcLayout;
import com.wang.avi.AVLoadingIndicatorView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class WallpapersActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageLoader imageLoader;
    private int mPosition;
    private String title;
    private final int EXTERNAL_STORAGE_ACCESS = 100;
    private final int EXTERNAL_STORAGE_ACCESS_SHARE = 101;
    private final int EXTERNAL_STORAGE_ACCESS_CROP = 102;
    private int task;
    private ArrayList<PictureModel> arrayList;
    private DisplayImageOptions displayImageOptions;
    View fab_button;
    View crop;
    View save_image;
    View menuLayout;
    View set_wallpaper;
    View share_this;
    View imageInfo;
    ArcLayout arcLayout;
    private ImageView mShuffleBtn;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpapers);

        Toolbar toolbar = findViewById(R.id.toolbar_wallpapers);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("hide_checked", true)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
        }

        mPosition = getIntent().getExtras().getInt("position");
        title = getIntent().getExtras().getString("title");
        arrayList = getIntent().getParcelableArrayListExtra("arrayList");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initImageLoader();

        initViews();
        loadAd();

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt("position");
        }

        initViewPager();

    }

    public void loadAd(){

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, Config.ADMOB_DOWNLOAD_DIALOG_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull final InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i("TAG", "onAdLoaded");
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        Log.d("TAG", "The ad was dismissed.");
                        loadAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when fullscreen content failed to show.
                        Log.d("TAG", "The ad failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        mInterstitialAd = null;
                        Log.d("TAG", "The ad was shown.");

                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i("TAG", loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
    }

    private void initViews() {
        fab_button = findViewById(R.id.fab_new);
        fab_button.setOnClickListener(this);

        set_wallpaper = findViewById(R.id.set_wallpaper);
        set_wallpaper.setOnClickListener(this);
        share_this = findViewById(R.id.share_image);
        share_this.setOnClickListener(this);
        crop = findViewById(R.id.crop_wall);
        crop.setOnClickListener(this);
        save_image = findViewById(R.id.save);
        save_image.setOnClickListener(this);
        imageInfo = findViewById(R.id.image_info);
        imageInfo.setOnClickListener(this);
        menuLayout = findViewById(R.id.menu_layout);
        menuLayout.setOnClickListener(this);
        arcLayout = (ArcLayout) findViewById(R.id.arc_layout);
        for (int i = 0, size = arcLayout.getChildCount(); i < size; i++) {
            arcLayout.getChildAt(i).setOnClickListener(this);
        }
        fab_button.setOnClickListener(this);

    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new CustomPagerAdapter(this, displayImageOptions);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(true, new ParallaxPagerTransformer(R.id.pager_imageView));
        viewPager.setCurrentItem(mPosition, true);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {


            case R.id.fab_new:
                if (v.isSelected()) {
                    hideMenu();
                } else {
                    showMenu();
                }
                v.setSelected(!v.isSelected());
                break;

            case R.id.set_wallpaper:
                fabSetWallpaper();
                onClick(fab_button);
                break;

            case R.id.share_image:
                shareProcess();
                onClick(fab_button);
                break;

            case R.id.save:
                downloadWallpaper();
                onClick(fab_button);
                break;

            case R.id.crop_wall:
                fabCropWall();
                onClick(fab_button);
                break;

            case R.id.image_info:
                imageInfoProcess();
                onClick(fab_button);
                break;
        }

    }

    private void fabCropWall() {
        if (DBHelper.getInstance().getURL(arrayList.get(mPosition).picOriginal)) {
            String mURI = DBHelper.getInstance().getImageURI(arrayList.get(mPosition).picOriginal);

            cropWallpaper(mURI);

        } else {
            if (Extra.isInternetON()) {
                    int hasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (Build.VERSION.SDK_INT < 29 && hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
                        task = 1;
                        ActivityCompat.requestPermissions(WallpapersActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                EXTERNAL_STORAGE_ACCESS_CROP);
                } else {
                    new AsyncDownloadCrop(1).execute(); // 1 = set wallpaper, 0 = download wallpaper, 2 = view wallpaper
                }
            } else {
                Extra.toast(getString(R.string.connect_to_internet));
            }
        }
    }
    public void cropWallpaper(String mURI) {
        String fName = "neon_image.jpg";
        File file = new File(getCacheDir(), fName);
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        File file1 = new File(mURI);
        UCrop.of(Uri.fromFile(file1), Uri.fromFile(file))
                .start(WallpapersActivity.this);

    }
    private void imageInfoProcess() {
        FragmentImageInfo imageInfo = new FragmentImageInfo();
        Bundle bundle = new Bundle();
        bundle.putString("image_id", arrayList.get(mPosition).imageID);
        imageInfo.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.ll_container, imageInfo, FragmentImageInfo.TAG).addToBackStack(null).commit();
    }

    @SuppressWarnings("NewApi")
    private void showMenu() {
        menuLayout.setVisibility(View.VISIBLE);

        List<Animator> animList = new ArrayList<>();

        for (int i = 0, len = arcLayout.getChildCount(); i < len; i++) {
            animList.add(createShowItemAnimator(arcLayout.getChildAt(i)));
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(400);
        animSet.setInterpolator(new OvershootInterpolator());
        animSet.playTogether(animList);
        animSet.start();
    }

    @SuppressWarnings("NewApi")
    private void hideMenu() {

        List<Animator> animList = new ArrayList<>();

        for (int i = arcLayout.getChildCount() - 1; i >= 0; i--) {
            animList.add(createHideItemAnimator(arcLayout.getChildAt(i)));
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(400);
        animSet.setInterpolator(new AnticipateInterpolator());
        animSet.playTogether(animList);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                menuLayout.setVisibility(View.INVISIBLE);
            }
        });
        animSet.start();

    }

    private Animator createShowItemAnimator(View item) {

        float dx = fab_button.getX() - item.getX();
        float dy = fab_button.getY() - item.getY();

        item.setRotation(0f);
        item.setTranslationX(dx);
        item.setTranslationY(dy);

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.rotation(0f, 720f),
                AnimatorUtils.translationX(dx, 0f),
                AnimatorUtils.translationY(dy, 0f)
        );

        return anim;
    }

    private Animator createHideItemAnimator(final View item) {
        float dx = fab_button.getX() - item.getX();
        float dy = fab_button.getY() - item.getY();

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.rotation(720f, 0f),
                AnimatorUtils.translationX(0f, dx),
                AnimatorUtils.translationY(0f, dy)
        );

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                item.setTranslationX(0f);
                item.setTranslationY(0f);
            }
        });

        return anim;
    }

    private void fabSetWallpaper() {
        if (DBHelper.getInstance().getURL(arrayList.get(mPosition).picOriginal)) {
            String mURI = DBHelper.getInstance().getImageURI(arrayList.get(mPosition).picOriginal);

            setAsWallpaper(mURI);

        } else {
            if (Extra.isInternetON()) {

                int hasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (Build.VERSION.SDK_INT < 29 && hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    task = 1;
                    ActivityCompat.requestPermissions(WallpapersActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            EXTERNAL_STORAGE_ACCESS);
                } else {
                    new AsyncDownload(1).execute(); // 1 = set wallpaper, 0 = download wallpaper, 2 = view wallpaper
                }
            } else {
                Extra.toast(getString(R.string.connect_to_internet));
            }
        }
    }

    private void downloadWallpaper() {
        if (DBHelper.getInstance().getURL(arrayList.get(mPosition).picOriginal)) {
            Toast.makeText(getApplicationContext(), R.string.image_already_saved, Toast.LENGTH_SHORT).show();
        } else {
            if (Extra.isInternetON()) {
                int hasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (Build.VERSION.SDK_INT < 29 && hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    task = 0;
                    ActivityCompat.requestPermissions(WallpapersActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            EXTERNAL_STORAGE_ACCESS);
                } else {
                    new AsyncDownload(0).execute();  // 1 = set wallpaper, 0 = download wallpaper, 2 = view wallpaper
                }

            } else {
                Extra.toast(getString(R.string.connect_to_internet));
            }

        }
    }
    /*
    private final void downloadImageToDownloadFolder() {
        Object var10000 = this.getSystemService(Context.Do);
        if (var10000 == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.app.DownloadManager");
        } else {
            DownloadManager mgr = (DownloadManager)var10000;
            Uri downloadUri = Uri.parse(downloadImageUrl);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(3).setAllowedOverRoaming(false).setTitle((CharSequence)"Sample").setDescription((CharSequence)"Sample Image Demo New").setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SampleImage.jpg");
            Toast.makeText(this.getApplicationContext(), (CharSequence)("Download successfully to " + (downloadUri != null ? downloadUri.getPath() : null)), 1).show();
            mgr.enqueue(request);
        }
    }*/
    private void initImageLoader() {
        displayImageOptions = Extra.imageDisplayOptionforDownload(this);
        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(Extra.imageLoaderConfig(this).build());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_ACCESS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new AsyncDownload(task).execute();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.not_enough_permission),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case EXTERNAL_STORAGE_ACCESS_SHARE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    shareThisImage();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.not_enough_permission),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case EXTERNAL_STORAGE_ACCESS_CROP:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new AsyncDownloadCrop(task).execute();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.not_enough_permission),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //    outState.putBoolean("fab", fabClicked);
        outState.putInt("position", mPosition);
    }
    @Override
    public void onResume(){
        super.onResume();
    /*    MyApplication myApp = (MyApplication)this.getApplication();
        if (myApp.wasInBackground)
        {
            //Do specific came-here-from-background code

            ShuffleDialogOnResume shuffleDialogOnResume = new ShuffleDialogOnResume(this);
            shuffleDialogOnResume.show();
        }

        myApp.stopActivityTransitionTimer();*/
    }

    @Override
    public void onPause(){
        super.onPause();
    //    ((MyApplication)this.getApplication()).startActivityTransitionTimer();
    }


    private void RatingCount() {
        SharedPreferences preferences = getSharedPreferences("Rate", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        int number = preferences.getInt("numberOfDownloads", 0);
        if (number > 3) {
            editor.putInt("numberOfDownloads", 0);
        } else {
            number++;
            editor.putInt("numberOfDownloads", number);
        }
        editor.apply();
    }

    public void saveImageToSDCard(String path, int pos, int work) {

        try {
            if (DBHelper.getInstance().insertData(arrayList.get(pos).picOriginal, path)) {
                Toast.makeText(getApplicationContext(), R.string.wallpaper_downloaded, Toast.LENGTH_SHORT).show();

            }

            if (work == 0) {

                if (mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                }

                RatingCount();

                SharedPreferences sharedPreferences = getSharedPreferences("Rate", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
                builder.title(getString(R.string.rate_me));
                builder.titleColorRes(R.color.main_bg);
                builder.content(R.string.rate_us);
                builder.contentColorRes(R.color.main_bg);
                builder.positiveText(getString(R.string.sure));
                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String uri = "market://details?id=" + getApplication().getPackageName();
                        intent.setData(Uri.parse(uri));
                        Intent chooser = Intent.createChooser(intent, getString(R.string.launch_playstore));
                        startActivity(chooser);
                        editor.putString("rated", "yes");
                        editor.apply();
                    }
                });


                builder.negativeText(getString(R.string.later));
                builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        editor.putString("rated", "later");
                        editor.apply();
                        dialog.cancel();
                    }
                });

                int number = sharedPreferences.getInt("numberOfDownloads", 3);
                String rated = sharedPreferences.getString("rated", "later");
                if (number == 1 && rated.equals("later")) {
                    builder.show();
                }
            }

            if (work == 1) {
                setAsWallpaper(path);
            }
            if (work == 2) {
                Intent intent = new Intent(getApplicationContext(), ViewPhotoActivity.class);
                intent.putExtra("imageLocation", path);
                intent.putExtra("title", title);
                startActivity(intent);

                if (mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                }
            }

        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }
    public void saveCropToSDCard(String path, int pos, int work) {
        try {
            if (DBHelper.getInstance().insertData(arrayList.get(pos).picOriginal, path)) {
                Toast.makeText(getApplicationContext(), R.string.wallpaper_downloaded, Toast.LENGTH_SHORT).show();

            }
            if (work == 1) {
                cropWallpaper(path);
            }

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void setAsWallpaper(String mURI) {
        File file = new File(mURI);
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(LegacyCompatFileProvider.getUri(this,file), "image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(intent, getString(R.string.set_as));
        this.startActivityForResult(chooser, 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final AVLoadingIndicatorView loadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.av_setting_wallpaper);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            assert resultUri != null;

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loadingIndicatorView.setVisibility(View.VISIBLE);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(WallpapersActivity.this);
                    try {
                        wallpaperManager.setBitmap(BitmapFactory.decodeFile(resultUri.getPath()));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Wallpaper is set successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    loadingIndicatorView.setVisibility(View.GONE);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelDownload() {
        
    }


    class AsyncDownload extends AsyncTask<String, Integer, String> { // force close direct here
        private int works;
        private int contentLength = -1;
        private int counter = 0;
        private MaterialDialog.Builder builder = null;
        private MaterialDialog dialog = null;
        private ProgressBar progressBar = null;
        private TextView textView = null;
        private int calculatedProgress = 0;
        private String path;
        private SharedPreferences sharedPreferences;

        AsyncDownload(int work) {
            this.works = work;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try {
                if (WallpapersActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    WallpapersActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    WallpapersActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }


            builder = new MaterialDialog.Builder(WallpapersActivity.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams")
            View view = inflater.inflate(R.layout.download_dialog, null);
            builder.backgroundColorRes(R.color.main_bg);
            builder.customView(view, false);
            builder.cancelable(false);
            builder.positiveText(R.string.cancel_text);
            builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    cancel(true);
                    WallpapersActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            });
/*            builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });*/
            textView = (TextView) view.findViewById(R.id.download_textView);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            progressBar.setIndeterminate(false);
            dialog = builder.build();
            dialog.show();

            sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        }

        @Override
        protected String doInBackground(String... params) {
            String as[] = null;

            URL url = null;
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                url = new URL(arrayList.get(mPosition).picOriginal);
            } catch (MalformedURLException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            try {

                File myDir = new File(getExternalFilesDir((String)null),"Wallpapers");

                //noinspection ResultOfMethodCallIgnored
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String timeStamp = new SimpleDateFormat("ss", Locale.US).format(new Date());
                String fname = getString(R.string.wallpapers) + "_" + n + "_" + timeStamp + ".jpg";
                File file = new File(myDir, fname);
                if (file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
                as = new String[1];
                as[0] = file.toString();
                path = file.getAbsolutePath();

                fileOutputStream = new FileOutputStream(file);
                MediaScannerConnection.scanFile(WallpapersActivity.this, as, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String s1, Uri uri)
                    {
                    }

                });
                assert url != null;
                httpURLConnection = (HttpURLConnection) url.openConnection();
                contentLength = httpURLConnection.getContentLength();
                inputStream = httpURLConnection.getInputStream();

                int read;
                byte[] buffer = new byte[1024];
                while ((read = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, read);
                    counter = counter + read;
                    publishProgress(counter);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.i("TAG", "Io exceotion" );
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (arrayList.get(mPosition).imageID != null) {
                HttpURLConnection urlConnection = null;
                try {
                    String dUrl = getString(R.string.download_count).replace("IMAGE_ID", arrayList.get(mPosition).imageID);
                    URL mUrl = new URL(dUrl);
                    urlConnection = (HttpURLConnection) mUrl.openConnection();
                    urlConnection.connect();
                    urlConnection.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            calculatedProgress = (int) (((double) values[0] / contentLength) * 100);
            textView.setText(String.format("%s%%", String.valueOf(calculatedProgress)));
            progressBar.setProgress(calculatedProgress);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            saveImageToSDCard(path, mPosition, works);
            dialog.dismiss(); // forec close here
            try {
                WallpapersActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }

    }

    class AsyncDownloadCrop extends AsyncTask<String, Integer, String> {
        private int works;
        private int contentLength = -1;
        private int counter = 0;
        private MaterialDialog.Builder builder = null;
        private MaterialDialog dialog = null;
        private ProgressBar progressBar = null;
        private TextView textView = null;
        private int calculatedProgress = 0;
        private String path;
        private SharedPreferences sharedPreferences;

        AsyncDownloadCrop(int work) {
            this.works = work;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try {
                if (WallpapersActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    WallpapersActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    WallpapersActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }


            builder = new MaterialDialog.Builder(WallpapersActivity.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams")
            View view = inflater.inflate(R.layout.download_dialog, null);
            builder.backgroundColorRes(R.color.main_bg);
            builder.customView(view, false);
            builder.cancelable(false);
            builder.positiveText(R.string.cancel_text);
            builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    cancel(true);
                    WallpapersActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            });
/*            builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });*/
            textView = (TextView) view.findViewById(R.id.download_textView);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            progressBar.setIndeterminate(false);
            dialog = builder.build();
            dialog.show();

            sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        }

        @Override
        protected String doInBackground(String... params) {

            String as [] = null;
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                url = new URL(arrayList.get(mPosition).picOriginal);
            } catch (MalformedURLException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            try {

                File myDir = new File(getExternalFilesDir((String)null),"Wallpapers");
                myDir.mkdirs();

                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String timeStamp = new SimpleDateFormat("ss", Locale.US).format(new Date());
                String fname = getString(R.string.wallpapers) + "_" + n + "_" + timeStamp + ".jpg";
                File file = new File(myDir, fname);

                if (file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
                as = new String[1];
                as[0] = file.toString();
                path = file.getAbsolutePath();

                fileOutputStream = new FileOutputStream(file);

                MediaScannerConnection.scanFile(WallpapersActivity.this, as, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String s1, Uri uri)
                    {
                    }

                });
                assert url != null;
                httpURLConnection = (HttpURLConnection) url.openConnection();
                contentLength = httpURLConnection.getContentLength();
                inputStream = httpURLConnection.getInputStream();

                int read;
                byte[] buffer = new byte[1024];
                while ((read = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, read);
                    counter = counter + read;
                    publishProgress(counter);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (arrayList.get(mPosition).imageID != null) {
                HttpURLConnection urlConnection = null;
                try {
                    String dUrl = getString(R.string.download_count).replace("IMAGE_ID", arrayList.get(mPosition).imageID);
                    URL mUrl = new URL(dUrl);
                    urlConnection = (HttpURLConnection) mUrl.openConnection();
                    urlConnection.connect();
                    urlConnection.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            calculatedProgress = (int) (((double) values[0] / contentLength) * 100);
            textView.setText(String.format("%s%%", String.valueOf(calculatedProgress)));
            progressBar.setProgress(calculatedProgress);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            saveCropToSDCard(path, mPosition, works);
            dialog.dismiss();
            try {
                WallpapersActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("listViewPosition", mPosition);
        editor.putBoolean("backPressed", true);
        editor.apply();
    }

    private void shareProcess() {
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(WallpapersActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT < 29 && hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WallpapersActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_ACCESS_SHARE);
        } else {
            shareThisImage();
        }
    }

    private void shareThisImage() {
        imageLoader.loadImage(arrayList.get(mPosition).picMedium, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                Extra.toast(getString(R.string.try_later));
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
              try {
                  Intent intent = new Intent(Intent.ACTION_SEND);
                  String path = MediaStore.Images.Media.insertImage(getContentResolver(), loadedImage, "", null);
                  intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                  intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path)); // force close here
                  intent.setType("image/*");
                  startActivity(Intent.createChooser(intent, getString(R.string.share_to)));
              }catch(Exception e){
                  Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
              }

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }


    // MY CUSTOM PAGER ADAPTER
    class CustomPagerAdapter extends PagerAdapter {


        private LayoutInflater mLayoutInflater;
        private Context mContext;
        private DisplayImageOptions dis;

        SharedPreferences preferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        private int layout;
        //private boolean long_checked;

        CustomPagerAdapter(Context context, DisplayImageOptions displayImageOptions) {

            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mContext = context;
            this.dis = displayImageOptions;
            if (preferences.getInt("preview_options", 0) == 0) {
                layout = R.layout.pager_item;
            } else {
                layout = R.layout.pager_item_1;
            }

            //long_checked = preferences.getBoolean("long_checked", false);
        }

        @Override
        public int getCount() {

            return arrayList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }


        @Override
        public Object instantiateItem(ViewGroup container, final int position) {


            View itemView = mLayoutInflater.inflate(layout, container, false);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.pager_imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                    if (DBHelper.getInstance().getURL(arrayList.get(mPosition).picOriginal)) {
                        Intent intent = new Intent(mContext, ViewPhotoActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("imageLocation", DBHelper.getInstance().getImageURI(arrayList.get(mPosition).picOriginal));
                        mContext.startActivity(intent);

                    } else {
                        if (Extra.isInternetON()) {

                            int hasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            if (Build.VERSION.SDK_INT < 29 && hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
                                task = 2;
                                ActivityCompat.requestPermissions(WallpapersActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        EXTERNAL_STORAGE_ACCESS);
                            }else {
                                new AsyncDownload(2).execute();
                            }
                        } else {
                            Extra.toast(getString(R.string.connect_to_internet));
                        }
                    }
                } catch (Exception e){
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

           try {
               String imageLoc = DBHelper.getInstance().getImageURI(arrayList.get(mPosition).picOriginal); // force close here
               if (imageLoc != null) {
                   File file = new File(imageLoc);
                   if (!(file.exists())) {
                       DBHelper.getInstance().deleteSingleRow(imageLoc);
                   }
               }

           }catch (Exception e){
               Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
           }
            imageLoader.displayImage(arrayList.get(position).picMedium, imageView, dis, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                }
            });

            container.addView(itemView);
            return itemView;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
        }
    }
    private void animateShuffleButton(final ImageView shuffleBtn) {
        final ViewPropertyAnimator mAnimator = shuffleBtn.animate();
        mAnimator.cancel();
        final int x = getResources().getDisplayMetrics().widthPixels;
        mAnimator.translationX(x).setListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                final AnimatorSet mAnimatorSet = new AnimatorSet();
                int pivotX = shuffleBtn.getWidth() / 2;
                int pivotY = shuffleBtn.getHeight() / 2;
                shuffleBtn.setPivotX(pivotX);
                shuffleBtn.setPivotY(pivotY);

                ObjectAnimator animator1 = ObjectAnimator.ofFloat(shuffleBtn, "translationX", new float[] { 0.0F });
                animator1.setDuration(1000L);
                animator1.setInterpolator(new DecelerateInterpolator());

                ObjectAnimator animator2 = ObjectAnimator.ofFloat(shuffleBtn, "rotation", new float[] { 0.0F, -25.0F, 25.0F,
                        -25.0F, 25.0F, 0.0F });
                animator2.setDuration(2000L);
                animator2.setInterpolator(new LinearInterpolator());

                ArrayList<Animator> animators = new ArrayList<Animator>();
                animators.add(animator1);
                animators.add(animator2);
                for (int i = 0; i < 20; i++) {
                    ObjectAnimator animator = animator2.clone();
                    animator.setStartDelay(1500L);
                    animators.add(animator);
                }
                mAnimatorSet.playSequentially(animators);
                mAnimatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        }).start();
    }
}
