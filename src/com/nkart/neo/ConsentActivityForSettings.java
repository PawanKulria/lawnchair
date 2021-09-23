package com.nkart.neo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.launcher3.settings.SettingsActivity;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.nkart.neo.extra.Extra;

import java.net.MalformedURLException;
import java.net.URL;

public class ConsentActivityForSettings extends Activity {
    ConsentForm consentForm;
    int internet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Extra.isInternetON()) {
            collectConsentInfo();
        } else
            startSettingsActivity();
    }


public  void collectConsentInfo(){
    final ConsentInformation consentInformation = ConsentInformation.getInstance(this);
   // ConsentInformation.getInstance(this).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
    String[] publisherIds = {"pub-8553297703763663"};
   // ConsentInformation.getInstance(getApplicationContext()).addTestDevice("F1F48DA734221EE385D1F6C00CCB685D");
    consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
        @Override
        public void onConsentInfoUpdated(ConsentStatus consentStatus) {
            // User's consent status successfully updated.
            ConsentInformation.getInstance(getApplicationContext()).isRequestLocationInEeaOrUnknown();
    //        Toast.makeText(getApplicationContext(), "User's consent status successfully updated.", Toast.LENGTH_SHORT).show();
            if(consentInformation.isRequestLocationInEeaOrUnknown()) {
                switch (consentStatus) {
                    case PERSONALIZED:
    //                    Toast.makeText(getApplicationContext(), "PERSONALIZED", Toast.LENGTH_SHORT).show();
                        startSettingsActivity();
                        break;
                    case NON_PERSONALIZED:
   //                     Toast.makeText(getApplicationContext(), "NON_PERSONALIZED", Toast.LENGTH_SHORT).show();
                        Log.e("Log", consentStatus.toString());
                        Bundle extras = new Bundle();
                        extras.putString("npa", "1");

                        AdRequest request = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .build();
                        startSettingsActivity();
                        break;
                    case UNKNOWN:
                        consentForm = makeConsentForm(ConsentActivityForSettings.this);
                        consentForm.load();
                        break;
                    default:
                        consentForm = makeConsentForm(ConsentActivityForSettings.this);
                        consentForm.load();
                        break;
                }
            }
            else
                startSettingsActivity();
        }

        @Override
        public void onFailedToUpdateConsentInfo(String errorDescription) {
            // User's consent status failed to update.
    //        Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
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
    //                    Toast.makeText(getApplicationContext(), "ConsentFormLoaded", Toast.LENGTH_SHORT).show();
                        consentForm.show();
                    }
                    @Override
                    public void onConsentFormOpened() {
                    }
                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
     //                   Toast.makeText(getApplicationContext(), "ConsentFormClosed", Toast.LENGTH_SHORT).show();
                        startSettingsActivity();
                    }
                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.e("Log", errorDescription);
        //                Toast.makeText(getApplicationContext(),errorDescription, Toast.LENGTH_LONG).show();
                    }
                })
                .withNonPersonalizedAdsOption()
                .withPersonalizedAdsOption()
                .build();
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(ConsentActivityForSettings.this, SettingsActivity.class);
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
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}