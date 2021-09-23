package com.nkart.neo.wallpapers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.launcher3.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.nkart.neo.utils.Config;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/*** Displays a splash screen image while and loads an interstitial before starting the application*/

public class SplashActivity extends Activity {

    private static final int WAIT_TIME = 8000;
    private InterstitialAd interstitialAd;
    private Timer waitTimer;
    private boolean interstitialCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    Log.d("MyApp", String.format(
                            "Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }

                // Start loading ads here...

            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, Config.ADMOB_SPLASH_WALLPAPER_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        SplashActivity.this.interstitialAd = interstitialAd;
                        Log.i("TAG", "onAdLoaded");
                        if (!interstitialCanceled) {
                            waitTimer.cancel();

                            interstitialAd.show(SplashActivity.this);
                        }
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");
                                SplashActivity.this.interstitialAd =null;
                                startMainActivity();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                                SplashActivity.this.interstitialAd =null;
                                startMainActivity();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                Log.d("TAG", "The ad was shown.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("TAG", loadAdError.getMessage());
                        interstitialAd = null;
                        startMainActivity();
                    }
                });

        waitTimer = new Timer();
        waitTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                interstitialCanceled = true;
                SplashActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
// The interstitial didn’t load in a reasonable amount of time. Stop waiting for the
// interstitial, and start the application.
                        startMainActivity();
                    }
                });
            }
        }, WAIT_TIME);
    }
    @Override
    public void onPause() {
// Flip the interstitialCanceled flag so that when the user comes back they aren’t stuck inside
// the splash screen activity.
        waitTimer.cancel();
        interstitialCanceled = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (interstitialAd != null) {
// The interstitial finished loading while the app was in the background. It’s up to you what
// the behavior should be when they return. In this example, we show the interstitial since
// it’s ready.
            interstitialAd.show(SplashActivity.this);
        } else if (interstitialCanceled) {
// There are two ways the user could get here:
//
// 1. After dismissing the interstitial
// 2. Pressing home and returning after the interstitial finished loading.
//
// In either case, it’s awkward to leave them in the splash screen activity, so just start the
// application.
            startMainActivity();
        }
    }
    /*** Starts the application’s {@link MainActivityWallpapers}.*/

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivityWallpapers.class);
        startActivity(intent);
        finish();
    }
}