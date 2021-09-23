package com.nkart.neo.wallpapers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.launcher3.R;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.nkart.neo.extra.Extra;
import com.nkart.neo.wallpapers.model.LegacyCompatFileProvider;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;


public class ViewPhotoActivity extends AppCompatActivity {

    private Boolean clicked = false;
    private String path;
    private int originalBrightness;
    private int brightnessMode;
    private boolean userLeft;
    private SubsamplingScaleImageView imageView;
    private KenBurnsView smartImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        boolean smart_mode = sharedPreferences.getBoolean("smart_mode", false);
        if (smart_mode) {
            setContentView(R.layout.activity_smart_view_photo);
        } else {
            setContentView(R.layout.activity_view_photo);
        }

        path = getIntent().getExtras().getString("imageLocation");

        if (smart_mode) {
            smartImageView = (KenBurnsView) findViewById(R.id.imageView3);
        } else {
            imageView = (SubsamplingScaleImageView) findViewById(R.id.imageView3);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);

        String title = getIntent().getExtras().getString("title");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (smart_mode) {
            ImageSize imageSize = new ImageSize(Extra.getScreenSizeInPixel(getApplicationContext(), "x") * 2,
                    Extra.getScreenSizeInPixel(getApplicationContext(), "y") * 2);
            com.nostra13.universalimageloader.core.ImageLoader imageLoader =
                    com.nostra13.universalimageloader.core.ImageLoader.getInstance();

            if (!imageLoader.isInited()) {
                imageLoader.init(Extra.imageLoaderConfig(getApplicationContext()).build());
            }

            imageLoader.loadImage("file:///" + path, imageSize, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    smartImageView.setImageBitmap(loadedImage);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });

            smartImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToolbarClick();
                }
            });

        } else {
            imageView.setImage(ImageSource.uri(path));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToolbarClick();
                }
            });
        }


        if (savedInstanceState != null) {
            originalBrightness = savedInstanceState.getInt("ORIGINAL_BRIGHTNESS");
            clicked = savedInstanceState.getBoolean("CLICKED");
            brightnessMode = savedInstanceState.getInt("BRIGHTNESS_MODE");
            if (clicked) {
                HideToolbar();
            } else {
                ShowToolbar();
            }
        }

    }

    private void ToolbarClick() {
        if (clicked) {
            ShowToolbar();
            clicked = false;
        } else {
            HideToolbar();
            clicked = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("ORIGINAL_BRIGHTNESS", originalBrightness);
        outState.putBoolean("CLICKED", clicked);
        outState.putInt("BRIGHTNESS_MODE", brightnessMode);

    }

    private void ShowToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();

            if (Build.VERSION.SDK_INT < 16) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                View decorView = getWindow().getDecorView();
                // Show the status bar.
                int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                decorView.setSystemUiVisibility(uiOptions);
            }
        }
    }

    private void HideToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();

            if (Build.VERSION.SDK_INT < 16) { //old method
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else { // Jellybean and up, new hotness
                View decorView = getWindow().getDecorView();
                // Hide the status bar.
                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* MyApplication myApp = (MyApplication)this.getApplication();
        if (myApp.wasInBackground)
        {
            //Do specific came-here-from-background code

            ShuffleDialogOnBack shuffleDialogOnResume = new ShuffleDialogOnBack(this);
            shuffleDialogOnResume.show();
        }

        myApp.stopActivityTransitionTimer();*/
    }
    @Override
    public void onPause(){
        super.onPause();
   //     ((MyApplication)this.getApplication()).startActivityTransitionTimer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        userLeft = true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.picture_shared), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.wallpaper_set), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_set_as_view) {
            setAsWallpaper(path);
        } else if (id == R.id.action_share_view) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
            Intent chooser = Intent.createChooser(intent, getString(R.string.set_as));
            startActivityForResult(chooser, 4);
        } else if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}
