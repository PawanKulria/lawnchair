package com.nkart.neo.extra;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.launcher3.R;
import com.nkart.neo.MyApplication;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.util.Objects;

import app.lawnchair.LawnchairApp;

/**
 * TODO: Created by Tanay on 12-08-2015.
 */
public class Extra {

    public static Typeface font = null;

    Extra() {
    }

    public static Boolean isInternetON() {
        ConnectivityManager connectivityManager = (ConnectivityManager) Objects.requireNonNull(LawnchairApp.getInstance()).getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void toast(String s) {
        Toast.makeText(MyApplication.getAppContext(), s, Toast.LENGTH_SHORT).show();
    }

    public static void mAlertDialogNoInternet(Context context) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.backgroundColorRes(R.color.colorPrimaryDarkTheme);
        builder.title(R.string.connectionRequired);
        builder.titleColorRes(R.color.applying_white);
        builder.content(R.string.connect_to_internet2);
        builder.contentColorRes(R.color.applying_white);
        builder.cancelable(false);
        builder.negativeText(R.string.later);
        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static ImageLoaderConfiguration.Builder imageLoaderConfig(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.denyCacheImageMultipleSizesInMemory();
        config.threadPriority(Thread.NORM_PRIORITY);
        config.diskCacheFileNameGenerator(new HashCodeFileNameGenerator());
        config.diskCacheSize(100 * 1024 * 1024); // 100 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        return config;
    }


    public static DisplayImageOptions imageDisplayOption(Context context) {
        return new DisplayImageOptions.Builder()
                .displayer(new SimpleBitmapDisplayer())
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .showImageOnLoading(ContextCompat.getDrawable(context, R.drawable.image_loading_bg))
                .showImageOnFail(ContextCompat.getDrawable(context, R.drawable.image_failed_bg))
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }
    public static DisplayImageOptions imageDisplayOptionCategory(Context context) {
        return new DisplayImageOptions.Builder()
                .displayer(new SimpleBitmapDisplayer())
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .showImageOnLoading(ContextCompat.getDrawable(context, R.drawable.image_cat_loading_bg))
                .showImageOnFail(ContextCompat.getDrawable(context, R.drawable.image_cat_failed_bg))
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }
    public static DisplayImageOptions imageDisplayOptionforDownload(Context context) {
        return new DisplayImageOptions.Builder()
                .displayer(new SimpleBitmapDisplayer())
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .showImageOnLoading(R.drawable.ic_stub2)
                .showImageOnFail(ContextCompat.getDrawable(context, R.drawable.image_failed_bg))
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }


    @SuppressWarnings("deprecation")
    public static int getScreenSizeInPixel(Context context, String s) {
        int value = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        int height, width;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            width = size.x;
            height = size.y;
        } else {
            width = display.getWidth();   // deprecated, suppressed for this method
            height = display.getHeight();  // deprecated, suppressed for this method
        }

        if (s.equals("x")) {
            value = width;
        } else if (s.equals("y")) {
            value = height;
        }
        return value;
    }
}
