package com.nkart.neo;

import android.app.Activity;
import android.content.Context;
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

import java.net.MalformedURLException;
import java.net.URL;

public class ConsentUpdateActivity extends Activity {
    ConsentForm consentForm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Extra.isInternetON()) {
            collectConsent();
        }
        else
            Toast.makeText(getApplicationContext(), "Internet connection is OFF", Toast.LENGTH_SHORT).show();

    }

    private void collectConsent() {
        final ConsentInformation consentInformation = ConsentInformation.getInstance(this);
     //   ConsentInformation.getInstance(this).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        String[] publisherIds = {"pub-8553297703763663"};
   //     ConsentInformation.getInstance(getApplicationContext()).addTestDevice("F1F48DA734221EE385D1F6C00CCB685D");
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.
                ConsentInformation.getInstance(getApplicationContext()).isRequestLocationInEeaOrUnknown();
        //        Toast.makeText(getApplicationContext(), "User's consent status successfully updated.", Toast.LENGTH_SHORT).show();
                if(consentInformation.isRequestLocationInEeaOrUnknown()) {
                    consentForm = makeConsentForm(ConsentUpdateActivity.this);
                    consentForm.load();
                } else {
                    Toast.makeText(getApplicationContext(), "Consent Not Required", Toast.LENGTH_SHORT).show();
                }
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

                        consentForm.show();
                    }
                    @Override
                    public void onConsentFormOpened() {
                    }
                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        //    if (consentStatus== NON_PERSONALIZED)
                        switch (consentStatus) {
                            case PERSONALIZED:
                                break;
                            case NON_PERSONALIZED:

                                Bundle extras = new Bundle();
                                extras.putString("npa", "1");

                                AdRequest request = new AdRequest.Builder()
                                        .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                        .build();

                                break;
                            case UNKNOWN:
                                break;
                            default:
                                break;
                        }
                        Toast.makeText(getApplicationContext(), "Consent Updated.Please press back button to continue", Toast.LENGTH_SHORT).show();

                    }
                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.e("Log", errorDescription);
                        Toast.makeText(getApplicationContext(),errorDescription,Toast.LENGTH_LONG).show();
                    }
                })
                .withNonPersonalizedAdsOption()
                .withPersonalizedAdsOption()
                .build();
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