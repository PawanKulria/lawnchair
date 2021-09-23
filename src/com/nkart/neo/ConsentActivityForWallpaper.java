package com.nkart.neo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.nkart.neo.extra.Extra;
import com.nkart.neo.wallpapers.SplashActivity;

import java.net.MalformedURLException;
import java.net.URL;

public class ConsentActivityForWallpaper extends Activity {
    ConsentForm consentForm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Extra.isInternetON()){
            collectConsentInfo();}
        else
            startSplashActivity();
          }
public  void collectConsentInfo(){
    final ConsentInformation consentInformation = ConsentInformation.getInstance(this);
 //   ConsentInformation.getInstance(this).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
    String[] publisherIds = {"pub-8553297703763663"};
   // ConsentInformation.getInstance(getApplicationContext()).addTestDevice("F1F48DA734221EE385D1F6C00CCB685D");
    consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
        @Override
        public void onConsentInfoUpdated(ConsentStatus consentStatus) {
            // User's consent status successfully updated.
            ConsentInformation.getInstance(getApplicationContext()).isRequestLocationInEeaOrUnknown();
        //    Toast.makeText(getApplicationContext(), "User's consent status successfully updated.", Toast.LENGTH_SHORT).show();
            if(consentInformation.isRequestLocationInEeaOrUnknown()) {
                switch (consentStatus) {
                    case PERSONALIZED:
        //                Toast.makeText(getApplicationContext(), "PERSONALIZED", Toast.LENGTH_SHORT).show();

                        startSplashActivity();
                        break;
                    case NON_PERSONALIZED:
                        Log.e("Log", consentStatus.toString());
                        Bundle extras = new Bundle();
                        extras.putString("npa", "1");

                        AdRequest request = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .build();
         //               Toast.makeText(getApplicationContext(), "non PERSONALIZED", Toast.LENGTH_SHORT).show();
                        startSplashActivity();
                        break;
                    case UNKNOWN:
                        consentForm = makeConsentForm(ConsentActivityForWallpaper.this);
                        consentForm.load();
           //             Toast.makeText(getApplicationContext(), "unknown", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        consentForm = makeConsentForm(ConsentActivityForWallpaper.this);
                        consentForm.load();
                        break;
                }
            }
            else
                startSplashActivity();

        }

        @Override
        public void onFailedToUpdateConsentInfo(String errorDescription) {
            // User's consent status failed to update.
            Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
        }
    });

}
    private ConsentForm makeConsentForm(Context context){
        URL privacyUrl = null;
        try {
            privacyUrl = new URL("http://pkulria.wixsite.com/nkart/single-post/2013/05/01/This-is-the-title-of-your-first-image-post");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new ConsentForm.Builder(context, privacyUrl)

                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
         //               Toast.makeText(getApplicationContext(), "ConsentFormLoaded", Toast.LENGTH_SHORT).show();
                        consentForm.show();
                    }
                    @Override
                    public void onConsentFormOpened() {
       //                 Toast.makeText(getApplicationContext(), "ConsentFormOpened", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        startSplashActivity();
                    }
                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.e("Log", errorDescription);
                        Toast.makeText(getApplicationContext(),errorDescription, Toast.LENGTH_LONG).show();
                    }
                })
                .withNonPersonalizedAdsOption()
                .withPersonalizedAdsOption()
                .build();
    }


    private void startSplashActivity() {
        Intent intent = new Intent(ConsentActivityForWallpaper.this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}