package com.nkart.neo.wallpapers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.nkart.neo.extra.Extra;
import com.nkart.neo.utils.Config;
import com.nkart.neo.wallpapers.db.DBHelper;
import com.nkart.neo.wallpapers.model.PictureModel;
import com.nkart.neo.wallpapers.model.RowType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class PicturesActivity extends AppCompatActivity {

    private static final String STRING_KEY = "key";
    private ArrayList<PictureModel> arrayList = new ArrayList<>();
    private ArrayList<PictureModel> arrayListPrevious = new ArrayList<>();
    private DisplayImageOptions displayImageOptions;
    private String title;
    private ImageLoader imageLoader;
    private RecyclerView recyclerView;
    private String noOfPage, page;
    private String albumId;
    private final int VIEW_NORMAL = 0;
    private final int VIEW_ADS = 1;
    private TanayPicturesAdapter adapter;
    private int count;
    private int resumeM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pictures);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            count = 3;
        } else {
            count = 2;
        }
        initViews();
        initUIL();
        loadHeaderImage();

        title = getIntent().getExtras().getString("albumName");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        albumId = getIntent().getExtras().getString("albumId");

        String url = getResources().getString(R.string.get_pictures);
        assert albumId != null;
        url = url.replace("CATEGORY_ID", albumId);
        url = url.replace("PAGE_NO", "0");

        String totalImages = getIntent().getExtras().getString("photos");

        assert totalImages != null;
        noOfPage = String.valueOf(Integer.parseInt(totalImages) / 30);

        if (savedInstanceState != null) {
            if (arrayListPrevious != null) {
                arrayListPrevious = savedInstanceState.getParcelableArrayList(STRING_KEY);
                handleAdView();
                adapter = new TanayPicturesAdapter(arrayList);
                setUpLayOutManager();
                recyclerView.setAdapter(adapter);
            }

            if (!Extra.isInternetON()) {
                Extra.toast(getString(R.string.connect_to_internet));
            }

        } else {
            if (Extra.isInternetON()) {
                VolleyRequest(url);
            }
        }
    }

    private void loadHeaderImage() {
        String photo_url = getIntent().getExtras().getString("photo_url");
        ImageView imageView = (ImageView) findViewById(R.id.imageView_pictures);
        imageLoader.displayImage(photo_url, imageView, displayImageOptions);
    }

    private void initUIL() {
        displayImageOptions = Extra.imageDisplayOption(this);
        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(Extra.imageLoaderConfig(this).build());
        }
    }

    private void initViews() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_pictures);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_pictures);

    }

    private void setUpLayOutManager() {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, count);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int x = 0;
                switch (adapter.getItemViewType(position)) {
                    case VIEW_NORMAL:
                        x = 1;
                        break;
                    case VIEW_ADS:
                        x = count;
                        break;
                }
                return x;
            }
        });

        recyclerView.setLayoutManager(gridLayoutManager);
    }


    public void VolleyRequest2(String url) {

        SharedPreferences sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        final int thumbnail = sharedPreferences.getInt("thumbnail", 0);
        final int preview = sharedPreferences.getInt("preview", 0);

        if (PicturesActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            PicturesActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            PicturesActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        RequestQueue queue = VolleySingleton.getInstance().getRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    JSONObject jsonObject1 = response.getJSONObject("result");

                    JSONObject paging = jsonObject1.getJSONObject("paging");
                    page = paging.getString("page");

                    JSONArray jsonArray = jsonObject1.getJSONArray("images");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        PictureModel pictureModel = new PictureModel(Parcel.obtain());
                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                        String urlOriginal = jsonObject2.getString("element_url");  // original url
                        pictureModel.favCount = jsonObject2.getString("hit");
                        pictureModel.pageUrl = jsonObject2.getString("page_url");
                        pictureModel.imageID = jsonObject2.getString("id");

                        JSONObject jsonObject3 = jsonObject2.getJSONObject("derivatives");


                        String th_url = null;
                        String pr_url = null;

                        if (thumbnail == 0) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("2small");
                            th_url = jsonObject4.getString("url");
                        } else if (thumbnail == 1) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("xsmall");
                            th_url = jsonObject4.getString("url");
                        } else if (thumbnail == 2) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("small");
                            th_url = jsonObject4.getString("url");
                        }


                        if (preview == 0) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("2small");
                            pr_url = jsonObject4.getString("url");

                        } else if (preview == 1) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("small");
                            pr_url = jsonObject4.getString("url");
                        } else if (preview == 2) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("large");
                            pr_url = jsonObject4.getString("url");
                        }


                        pictureModel.url = th_url;
                        pictureModel.picOriginal = urlOriginal;
                        pictureModel.picMedium = pr_url;
                        arrayListPrevious.add(pictureModel);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handleAdView();

                adapter.notifyDataSetChanged();

                PicturesActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Extra.toast(getString(R.string.error_message));

                PicturesActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

            }
        });

        queue.add(jsonObjectRequest);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        page = savedInstanceState.getString("PAGE");
        noOfPage = savedInstanceState.getString("NO_OF_PAGE");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STRING_KEY, arrayListPrevious);
        outState.putString("PAGE", page);
        outState.putString("NO_OF_PAGE", noOfPage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean backed = sharedPreferences.getBoolean("backPressed", false);
        if (backed) {
            recyclerView.smoothScrollToPosition(sharedPreferences.getInt("listViewPosition", 0) + resumeM);
        }
        editor.putInt("listViewPosition", 0);
        editor.putBoolean("backPressed", false);
        editor.apply();
      /*  MyApplication myApp = (MyApplication)this.getApplication();
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
    //    ((MyApplication)this.getApplication()).startActivityTransitionTimer();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void VolleyRequest(String url) {

        if (PicturesActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            PicturesActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            PicturesActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        final int thumbnail = sharedPreferences.getInt("thumbnail", 0);
        final int preview = sharedPreferences.getInt("preview", 0);

    //    final AVLoadingIndicatorView progressWheel = (AVLoadingIndicatorView) findViewById(R.id.av_loading_pictures);
    //    progressWheel.setVisibility(View.VISIBLE);

        RequestQueue queue = VolleySingleton.getInstance().getRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    JSONObject jsonObject1 = response.getJSONObject("result");

                    JSONObject paging = jsonObject1.getJSONObject("paging");

                    page = paging.getString("page");

                    JSONArray jsonArray = jsonObject1.getJSONArray("images");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        PictureModel pictureModel = new PictureModel(Parcel.obtain());
                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                        String urlOriginal = jsonObject2.getString("element_url");  // original url
                        pictureModel.favCount = jsonObject2.getString("hit");
                        pictureModel.pageUrl = jsonObject2.getString("page_url");
                        pictureModel.imageID = jsonObject2.getString("id");

                        JSONObject jsonObject3 = jsonObject2.getJSONObject("derivatives");

                        String th_url = null;
                        String pr_url = null;

                        if (thumbnail == 0) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("2small");
                            th_url = jsonObject4.getString("url");
                        } else if (thumbnail == 1) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("xsmall");
                            th_url = jsonObject4.getString("url");
                        } else if (thumbnail == 2) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("small");
                            th_url = jsonObject4.getString("url");
                        }


                        if (preview == 0) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("2small");
                            pr_url = jsonObject4.getString("url");

                        } else if (preview == 1) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("small");
                            pr_url = jsonObject4.getString("url");
                        } else if (preview == 2) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("large");
                            pr_url = jsonObject4.getString("url");
                        }

                        pictureModel.url = th_url;
                        pictureModel.picOriginal = urlOriginal;
                        pictureModel.picMedium = pr_url;
                        arrayListPrevious.add(pictureModel);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

    //            progressWheel.setVisibility(View.GONE);
                PicturesActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

                handleAdView(); // adViewHandle

                adapter = new TanayPicturesAdapter(arrayList);
                recyclerView.setAdapter(adapter);

                setUpLayOutManager();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Extra.toast(getString(R.string.error_message));
    //            progressWheel.setVisibility(View.GONE);
            }
        });

        queue.add(jsonObjectRequest);
    }

    private void handleAdView() {
        arrayList.clear();
        int y = 1;
        for (int i = 0; i < arrayListPrevious.size(); i++) {
            PictureModel model = new PictureModel(Parcel.obtain());

            if (i == (5 * count * y)) {
                model.rowType = RowType.ADS;
                arrayList.add(model);
                y++;
                i--;
            } else {
                model.url = arrayListPrevious.get(i).url;
                model.favCount = arrayListPrevious.get(i).favCount;
                model.imageID = arrayListPrevious.get(i).imageID;
                model.pageUrl = arrayListPrevious.get(i).pageUrl;
                model.rowType = RowType.NORMAL;
                model.picMedium = arrayListPrevious.get(i).picMedium;
                model.picOriginal = arrayListPrevious.get(i).picOriginal;
                arrayList.add(model);
            }
        }
    }


    private class TanayPicturesAdapter extends RecyclerView.Adapter<TanayPicturesAdapter.MainViewHOlder> {
        private ArrayList<PictureModel> arrayList;
        private int layout;
        private SharedPreferences sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        private boolean smart_mode = sharedPreferences.getBoolean("smart_mode", false);


        TanayPicturesAdapter(ArrayList<PictureModel> data) {
            this.arrayList = data;
            if (smart_mode) {
                layout = R.layout.single_row_smart_picture;
            } else {
                layout = R.layout.single_row_pictures;
            }
        }

        @Override
        public MainViewHOlder onCreateViewHolder(ViewGroup parent, int viewType) {
            return viewType == VIEW_NORMAL ? new MyViewHolder(getLayoutInflater().inflate(layout, parent, false)) : new AdsViewHolder(getLayoutInflater().inflate(R.layout.ad_unit_native, parent, false));
        }

        @Override
        public void onBindViewHolder(MainViewHOlder holder, int position) {

            if (arrayList.get(position).rowType.equals(RowType.NORMAL)) {
                MyViewHolder myViewHolder = (MyViewHolder) holder;

                if (DBHelper.getInstance().getFavorite(arrayList.get(position).picMedium)) {
                    myViewHolder.favCount.setCompoundDrawablesWithIntrinsicBounds(ActivityCompat.getDrawable(PicturesActivity.this, R.drawable.fav_x), null, null, null);
                } else {
                    myViewHolder.favCount.setCompoundDrawablesWithIntrinsicBounds(ActivityCompat.getDrawable(PicturesActivity.this, R.drawable.no_fav_x), null, null, null);
                }

                imageLoader.displayImage(arrayList.get(position).url, myViewHolder.image, displayImageOptions);
                myViewHolder.favCount.setText(arrayList.get(position).favCount);
            //    Extra.setTypeface(myViewHolder.favCount);
            } else {
                AdsViewHolder adsViewHolder = (AdsViewHolder) holder;
                LoadAdMobNativeAd(adsViewHolder);
            }

            try {
                // load next page
                if (position == arrayList.size() - 4) {
                    int currentPage = Integer.parseInt(page) + 1;
                    String url = getString(R.string.get_pictures);
                    url = url.replace("CATEGORY_ID", albumId);
                    url = url.replace("PAGE_NO", String.valueOf(currentPage));
                    if (currentPage <= Integer.parseInt(noOfPage)) {
                        if (Extra.isInternetON()) {
                            VolleyRequest2(url);
                        }
                    }
                }
            }catch (Exception e)
            {
                Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        private void LoadAdMobNativeAd(final AdsViewHolder adsViewHolder) {

            AdLoader.Builder builder = new AdLoader.Builder(PicturesActivity.this, Config.ADMOB_GRID_ID);

            builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                // OnNativeAdLoadedListener implementation.
                @Override
                public void onNativeAdLoaded(NativeAd NativeAd) {
                    FrameLayout frameLayout = adsViewHolder.adView;
                    NativeAdView adView = (NativeAdView) getLayoutInflater()
                            .inflate(R.layout.ad_unified, null);
                    populateNativeAdView(NativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }

            });

            VideoOptions videoOptions = new VideoOptions.Builder()
                    .build();

            NativeAdOptions adOptions = new NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .build();

            builder.withNativeAdOptions(adOptions);

            AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(LoadAdError errorCode) {
                }
            }).build();

            adLoader.loadAd(new AdRequest.Builder().build());
        }

        /**
         * Populates a {@link NativeAdView} object with data from a given
         * {@link NativeAd}.
         *
         * @param nativeAd the object containing the ad's assets
         * @param adView          the view to be populated
         */
        private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
            // Set the media view. Media content will be automatically populated in the media view once
            // adView.setNativeAd() is called.
            MediaView mediaView = adView.findViewById(R.id.ad_media);
            adView.setMediaView(mediaView);

            // Set other ad assets.
            adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
            adView.setBodyView(adView.findViewById(R.id.ad_body));
            adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
            adView.setIconView(adView.findViewById(R.id.ad_app_icon));
            adView.setPriceView(adView.findViewById(R.id.ad_price));
            adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
            adView.setStoreView(adView.findViewById(R.id.ad_store));
            adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

            // The headline is guaranteed to be in every NativeAd.
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

            // These assets aren't guaranteed to be in every NativeAd, so it's important to
            // check before trying to display them.
            if (nativeAd.getBody() == null) {
                adView.getBodyView().setVisibility(View.INVISIBLE);
            } else {
                adView.getBodyView().setVisibility(View.VISIBLE);
                ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
            }

            if (nativeAd.getCallToAction() == null) {
                adView.getCallToActionView().setVisibility(View.INVISIBLE);
            } else {
                adView.getCallToActionView().setVisibility(View.VISIBLE);
                ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }

            if (nativeAd.getIcon() == null) {
                adView.getIconView().setVisibility(View.GONE);
            } else {
                ((ImageView) adView.getIconView()).setImageDrawable(
                        nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }

            if (nativeAd.getPrice() == null) {
                adView.getPriceView().setVisibility(View.INVISIBLE);
            } else {
                adView.getPriceView().setVisibility(View.VISIBLE);
                ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
            }

            if (nativeAd.getStore() == null) {
                adView.getStoreView().setVisibility(View.INVISIBLE);
            } else {
                adView.getStoreView().setVisibility(View.VISIBLE);
                ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
            }

            if (nativeAd.getStarRating() == null) {
                adView.getStarRatingView().setVisibility(View.INVISIBLE);
            } else {
                ((RatingBar) adView.getStarRatingView())
                        .setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }

            if (nativeAd.getAdvertiser() == null) {
                adView.getAdvertiserView().setVisibility(View.INVISIBLE);
            } else {
                ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
            }

            // This method tells the Google Mobile Ads SDK that you have finished populating your
            // native ad view with this native ad. The SDK will populate the adView's MediaView
            // with the media content from this native ad.
            adView.setNativeAd(nativeAd);

            // Get the video controller for the ad. One will always be provided, even if the ad doesn't
            // have a video asset.
            VideoController vc = nativeAd.getMediaContent().getVideoController();

            // Updates the UI to say whether or not this ad has a video asset.
            if (vc.hasVideoContent()) {

                // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                // VideoController will call methods on this object when events occur in the video
                // lifecycle.
                vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                    @Override
                    public void onVideoEnd() {
                        // Publishers should allow native ads to complete video playback before
                        // refreshing or replacing them with another ad in the same UI location.
                        //        refresh.setEnabled(true);
                        //        videoStatus.setText("Video status: Video playback has ended.");
                        super.onVideoEnd();
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return arrayList.get(position).rowType.equals(RowType.ADS) ? VIEW_ADS : VIEW_NORMAL;
        }


        class MyViewHolder extends MainViewHOlder implements View.OnClickListener {
            ImageView image;
            TextView favCount;

            MyViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.imageView2);
                favCount = (TextView) itemView.findViewById(R.id.text_fav);
                image.setOnClickListener(this);
                favCount.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.text_fav:
                        if (DBHelper.getInstance().getFavorite(arrayList.get(getLayoutPosition()).picMedium)) {
                            DBHelper.getInstance().removeFromFavorites(arrayList.get(getLayoutPosition()).picMedium);
                            PictureModel picModel = arrayList.get(getLayoutPosition());
                            picModel.favCount = String.valueOf(Integer.parseInt(picModel.favCount) - 1);
                            arrayList.set(getLayoutPosition(), picModel);
                            adapter.notifyItemChanged(getLayoutPosition());
                        } else {
                            new AsyncTask<Void, Void, Void>() { //force close direct here
                                @Override
                                protected Void doInBackground(Void... params) {

                                    HttpURLConnection urlConnection = null;
                                    try {
                                        URL url = new URL(arrayList.get(getLayoutPosition()).pageUrl);
                                        urlConnection = (HttpURLConnection) url.openConnection();
                                        urlConnection.connect();
                                        urlConnection.getResponseCode();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } finally {
                                        if (urlConnection != null) {
                                            urlConnection.disconnect();
                                        }
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    try{
                                        super.onPostExecute(aVoid); //force close on bottom line
                                    DBHelper.getInstance().addToFavorites(arrayList.get(getLayoutPosition()).picMedium, arrayList.get(getLayoutPosition()).picOriginal);
                                    PictureModel picModel = arrayList.get(getLayoutPosition());
                                    picModel.favCount = String.valueOf(Integer.parseInt(picModel.favCount) + 1);
                                    arrayList.set(getLayoutPosition(), picModel);
                                    adapter.notifyItemChanged(getLayoutPosition());}
                                    catch(Exception e)
                                    {
                                        Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                        break;
                    case R.id.imageView2:

                        int m = 0;
                        for (int i = 0; i <= getLayoutPosition(); i++) {
                            if (arrayList.get(i).rowType.equals(RowType.ADS)) {
                                m++;
                            }
                        }

                        resumeM = m;

                        Intent intent = new Intent(PicturesActivity.this, WallpapersActivity.class);
                        intent.putExtra("position", getLayoutPosition() - m);
                        intent.putExtra("title", title);
                        intent.putParcelableArrayListExtra("arrayList", arrayListPrevious);

                        if (Extra.isInternetON()) {
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        } else {
                            Extra.toast(getString(R.string.connect_to_internet));
                        }
                        break;
                }
            }
        }


        private class AdsViewHolder extends MainViewHOlder {
            FrameLayout adView;

            AdsViewHolder(View itemView) {
                super(itemView);
                adView = (FrameLayout) itemView.findViewById(R.id.fl_adplaceholder);
            }
        }

        class MainViewHOlder extends RecyclerView.ViewHolder {

            MainViewHOlder(View itemView) {
                super(itemView);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wallpapers, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }

    }

}