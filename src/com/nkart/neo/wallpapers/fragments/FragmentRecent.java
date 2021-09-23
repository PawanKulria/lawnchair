package com.nkart.neo.wallpapers.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
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
import com.nkart.neo.wallpapers.VolleySingleton;
import com.nkart.neo.wallpapers.WallpapersActivity;
import com.nkart.neo.wallpapers.db.DBHelper;
import com.nkart.neo.wallpapers.model.PictureModel;
import com.nkart.neo.wallpapers.model.RowType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentRecent extends Fragment {

    private static final String STRING_KEY = "keyRecent";
    private DisplayImageOptions displayImageOptions;
    private ArrayList<PictureModel> arrayList = new ArrayList<>();
    private ArrayList<PictureModel> arrayListPrevious = new ArrayList<>();
    private ImageLoader imageLoader;
    private RecyclerView recyclerView;
    private AVLoadingIndicatorView progressWheel;
    private TanayRecentAdapter adapter;
    private final int VIEW_NORMAL = 0;
    private final int VIEW_ADS = 1;
    private int count;
    private int resumeM;

    public FragmentRecent() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent, container, false);
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            count = 3;
        } else {
            count = 2;
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_recent);

        setUpLayOutManager();

        TextView textViewNothing = (TextView) view.findViewById(R.id.noConnectionText);
        ImageView imageViewNothing = (ImageView) view.findViewById(R.id.noConnectionImage);

        if (!Extra.isInternetON()) {
            imageViewNothing.setVisibility(View.VISIBLE);
            textViewNothing.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        displayImageOptions = Extra.imageDisplayOption(getContext());

        progressWheel = (AVLoadingIndicatorView) getActivity().findViewById(R.id.av_loading);
        // Initialize ImageLoader with configuration.
        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(Extra.imageLoaderConfig(getContext()).build());
        }
    }

    private void setUpLayOutManager() {

        if (getActivity() != null) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), count);
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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            if (arrayListPrevious != null) {
                arrayListPrevious = savedInstanceState.getParcelableArrayList(STRING_KEY);
                handleAdView();
                adapter = new TanayRecentAdapter(arrayList);
                setUpLayOutManager();
                recyclerView.setAdapter(adapter);
            }

            if (!Extra.isInternetON()) {
                Extra.toast(getString(R.string.connect_to_internet));
            }

        } else {
            if (Extra.isInternetON()) {
                VolleyRequest();
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        System.gc();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STRING_KEY, arrayListPrevious);
    }


    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean backed = sharedPreferences.getBoolean("backPressed", false);
        if (backed) {
            recyclerView.smoothScrollToPosition(sharedPreferences.getInt("listViewPosition", 0) + resumeM);
        }

        editor.putInt("listViewPosition", 0);
        editor.putBoolean("backPressed", false);
        editor.apply();
    }

    private void VolleyRequest() {

        try {
            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        progressWheel.setVisibility(View.VISIBLE);

        String url = getResources().getString(R.string.recent_url);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        final int thumbnail = sharedPreferences.getInt("thumbnail", 0);
        final int preview = sharedPreferences.getInt("preview", 0);

        RequestQueue queue = VolleySingleton.getInstance().getRequestQueue();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject jsonObject1 = response.getJSONObject("result");
                    JSONArray jsonArray = jsonObject1.getJSONArray("images");

                    int length = jsonArray.length();

                    for (int i = 0; i < length; i++) {
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
                        pictureModel.picMedium = pr_url;
                        pictureModel.picOriginal = urlOriginal;


                        arrayListPrevious.add(pictureModel);


                        handleAdView(); // adViewHandle

                        adapter = new TanayRecentAdapter(arrayList);
                        recyclerView.setAdapter(adapter);

                        setUpLayOutManager();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progressWheel.setVisibility(View.GONE);

                try {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                    if (isAdded()) {
                    progressWheel.setVisibility(View.GONE);
                    try {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    Extra.toast(getString(R.string.error_message));
                }

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
                model.pageUrl = arrayListPrevious.get(i).pageUrl;
                model.rowType = RowType.NORMAL;
                model.imageID = arrayListPrevious.get(i).imageID;
                model.picMedium = arrayListPrevious.get(i).picMedium;
                model.picOriginal = arrayListPrevious.get(i).picOriginal;
                arrayList.add(model);
            }
        }
    }


    private class TanayRecentAdapter extends RecyclerView.Adapter<TanayRecentAdapter.MainViewHOlder> {
        private ArrayList<PictureModel> arrayList;

        TanayRecentAdapter(ArrayList<PictureModel> data) {
            this.arrayList = data;
        }

        @Override
        public MainViewHOlder onCreateViewHolder(ViewGroup parent, int viewType) {
            return viewType == VIEW_NORMAL ? new MyViewHolder(getActivity().getLayoutInflater().inflate(R.layout.single_row_pictures, parent, false)) : new AdsViewHolder(getActivity().getLayoutInflater().inflate(R.layout.ad_unit_native, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return arrayList.get(position).rowType.equals(RowType.ADS) ? VIEW_ADS : VIEW_NORMAL;
        }

        @Override
        public void onBindViewHolder(MainViewHOlder mainViewHOlder, int position) {

            if (arrayList.get(position).rowType.equals(RowType.NORMAL)) {
                MyViewHolder holder = (MyViewHolder) mainViewHOlder;

                if (DBHelper.getInstance().getFavorite(arrayList.get(position).picMedium)) {
                    holder.favCount.setCompoundDrawablesWithIntrinsicBounds(ActivityCompat.getDrawable(getContext(), R.drawable.fav_x), null, null, null);
                } else {
                    holder.favCount.setCompoundDrawablesWithIntrinsicBounds(ActivityCompat.getDrawable(getContext(), R.drawable.no_fav_x), null, null, null);
                }

                imageLoader.displayImage(arrayList.get(position).url, holder.image, displayImageOptions);
                holder.favCount.setText(arrayList.get(position).favCount);
             //   Extra.setTypeface(holder.favCount);
            } else {

                AdsViewHolder adsViewHolder = (AdsViewHolder) mainViewHOlder;
                LoadAdMobNativeAd(adsViewHolder);
            }


        }

        private void LoadAdMobNativeAd(final AdsViewHolder adsViewHolder) {

            AdLoader.Builder builder = new AdLoader.Builder(getActivity(), Config.ADMOB_GRID_ID);

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

        class AdsViewHolder extends MainViewHOlder {

            FrameLayout adView;

            AdsViewHolder(View itemView) {
                super(itemView);
                adView = (FrameLayout) itemView.findViewById(R.id.fl_adplaceholder);
            }
        }


        class MyViewHolder extends MainViewHOlder implements View.OnClickListener {
            private ImageView image;
            private TextView favCount;

            MyViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.imageView2);
                favCount = (TextView) itemView.findViewById(R.id.text_fav);
                favCount.setOnClickListener(this);
                image.setOnClickListener(this);
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.imageView2:

                        int m = 0;
                        for (int i = 0; i <= getLayoutPosition(); i++) {
                            if (arrayList.get(i).rowType.equals(RowType.ADS)) {
                                m++;
                            }
                        }
                        resumeM = m;

                        Intent intent = new Intent(getActivity(), WallpapersActivity.class);
                        intent.putParcelableArrayListExtra("arrayList", arrayListPrevious);
                        intent.putExtra("position", getLayoutPosition() - m);
                        intent.putExtra("title", "Recent");
                        if (Extra.isInternetON()) {
                            startActivity(intent);
                        } else {
                            Extra.toast(getString(R.string.connect_to_internet));
                        }
                        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                        break;

                    case R.id.text_fav:

                    /*    if (swipeRefreshLayout.isRefreshing()) {
                            return;
                        }*/

                        if (DBHelper.getInstance().getFavorite(arrayList.get(getLayoutPosition()).picMedium)) {
                            DBHelper.getInstance().removeFromFavorites(arrayList.get(getLayoutPosition()).picMedium);
                            PictureModel picModel = arrayList.get(getLayoutPosition());
                            picModel.favCount = String.valueOf(Integer.parseInt(picModel.favCount) - 1);
                            arrayList.set(getLayoutPosition(), picModel);
                            adapter.notifyItemChanged(getLayoutPosition());
                        } else {

                            new AsyncTask<Void, Void, Void>() { // force close direct here
                                @Override
                                protected Void doInBackground(Void... params) {

                                    HttpURLConnection urlConnection = null;
                                    try {
                                        URL url = new URL(arrayList.get(getLayoutPosition()).pageUrl);
                                        urlConnection = (HttpURLConnection) url.openConnection();
                                        urlConnection.connect();
                                        urlConnection.getResponseCode();
                                    } catch (Exception e) {
                                        Toast.makeText(getContext(), "Sorry, please try again", Toast.LENGTH_SHORT).show();

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
                                    super.onPostExecute(aVoid); // force close on bottom line
                                    DBHelper.getInstance().addToFavorites(arrayList.get(getLayoutPosition()).picMedium, arrayList.get(getLayoutPosition()).picOriginal);
                                    PictureModel picModel = arrayList.get(getLayoutPosition());
                                    picModel.favCount = String.valueOf(Integer.parseInt(picModel.favCount) + 1);
                                    arrayList.set(getLayoutPosition(), picModel);
                                    adapter.notifyItemChanged(getLayoutPosition());}
                                    catch(Exception e)
                                    {
                                        Toast.makeText(getContext(), "Sorry, please try again", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }


                        break;
                }

            }
        }

        class MainViewHOlder extends RecyclerView.ViewHolder {

            MainViewHOlder(View itemView) {
                super(itemView);
            }
        }
    }


}
