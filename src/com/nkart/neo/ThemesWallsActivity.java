package com.nkart.neo;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.launcher3.R;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.nkart.neo.extra.Extra;
import com.nkart.neo.utils.Config;
import com.nkart.neo.utils.Constant;
import com.nkart.neo.utils.JsonUtils;
import com.nkart.neo.wallpapers.app.BaseTask;
import com.nkart.neo.wallpapers.app.TaskRunner;
import com.nkart.neo.wallpapers.model.ItemSlider;
import com.nkart.neo.wallpapers.model.ItemTheme;
import com.nkart.neo.wallpapers.model.ShimmerFrameLayout;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ThemesWallsActivity extends AppCompatActivity
		implements BaseSliderView.OnSliderClickListener  {

	private String mThemeName;
	private SliderLayout mSlider;
	ItemSlider itemSlider;
	List<ItemSlider> arrayofSlider;
	private ImageLoader imageLoader;

	List<ItemTheme> arrayOfRingtone;
	private LinearLayout mThemeContainer;
	ItemTheme itemRingtone;
	private int mThemeImageSize;
	private DisplayImageOptions options;
	private ItemTheme objAllBean;

	private ShimmerFrameLayout shimmerFrameLayout;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.themes_walls_layout);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(R.string.home_themes);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}

		imageLoader = ImageLoader.getInstance();

		if (!imageLoader.isInited()) {
			imageLoader.init(Extra.imageLoaderConfig(this).build());
		}

		initView();

		mThemeImageSize = getResources().getDimensionPixelSize(R.dimen.slider_height);
		arrayOfRingtone = new ArrayList<>();
	}


	private void initView() {
		mThemeContainer = findViewById(R.id.theme_image_container);
		mSlider = findViewById(R.id.slider);
		arrayofSlider=new ArrayList<>();

		shimmerFrameLayout = findViewById(R.id.shimmer);

		if (Extra.isInternetON()){
			LoadAdMobNativeAd();
			TaskRunner runner = new TaskRunner();
			runner.executeAsync(new MyRingTone(Constant.URL_THEME));

			TaskRunner runner1 = new TaskRunner();
			runner1.executeAsync(new MyTaskFeatured(Constant.URL_SLIDER));
		}
	}


	private void LoadAdMobNativeAd() {

		AdLoader.Builder builder = new AdLoader.Builder(this, Config.ADMOB_THEMES_ID);

		builder.forNativeAd(
				new com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener() {
					@Override
					public void onNativeAdLoaded(com.google.android.gms.ads.nativead.NativeAd nativeAd) {

						shimmerFrameLayout.stopShimmerAnimation();
						shimmerFrameLayout.setVisibility(View.GONE);
						try {
							FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
							NativeAdView adView = (NativeAdView) getLayoutInflater()
									.inflate(R.layout.ad_unified, null);
							populateNativeAdView(nativeAd, adView);
							frameLayout.removeAllViews();
							frameLayout.addView(adView);
						} catch (Exception e){

						}
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
			public void onAdFailedToLoad(LoadAdError loadAdError) {

				shimmerFrameLayout.stopShimmerAnimation();
				shimmerFrameLayout.setVisibility(View.GONE);
			}

			public void onAdLoaded() {

			}
		}).build();

		adLoader.loadAd(new AdRequest.Builder().build());
	}

	/**
	 * Populates a {@link NativeAdView} object with data from a given
	 * {@link com.google.android.gms.ads.nativead.NativeAd}.
	 *
	 * @param nativeAd the object containing the ad's assets
	 * @param adView          the view to be populated
	 */
	private void populateNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView adView) {
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

		// The headline and mediaContent are guaranteed to be in every NativeAd.
		((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
		adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

		// These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
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
	public void onSliderClick(BaseSliderView slider) {
		startActivity(new Intent(
				Intent.ACTION_VIEW,
				Uri.parse(slider.getBundle().getString("extra"))));
	}

	private class MyTaskFeatured extends BaseTask<String> {

		String params;

		MyTaskFeatured(String params) {
			this.params = params;
		}

		@Override
		public String call() {
			return JsonUtils.getJSONString(params);
		}

		@Override
		public void onMyPostExecute(String result) {
			super.onMyPostExecute(result);
			if (null == result || result.length() == 0) {
				//    showToast(getString(R.string.nodata));

			} else {

				try {
					JSONObject mainJson = new JSONObject(result);
					JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);
					JSONObject objJson = null;
					for (int i = 0; i < jsonArray.length(); i++) {
						objJson = jsonArray.getJSONObject(i);


						String id = objJson.getString(Constant.TAG_ID);
						String name = objJson.getString(Constant.TAG_SLIDER_NAME);
						String image = objJson.getString(Constant.TAG_SLIDER_IMAGE);
						String imageThumb = objJson.getString(Constant.TAG_SLIDER_IMAGE_THUMB);
						String url = objJson.getString(Constant.TAG_SLIDER_URL);
						ItemSlider objItem = new ItemSlider(id, name, image, imageThumb, url);
						arrayofSlider.add(objItem);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

				setAdapterToFeatured();
			}
		}
	}

	public void setAdapterToFeatured() {

		for(int i=0;i<arrayofSlider.size();i++)
		{
			itemSlider=arrayofSlider.get(i);
			TextSliderView textSliderView = new TextSliderView(this);
			textSliderView.description(itemSlider.getName());
			textSliderView.image(itemSlider.getImage().replace(" ", "%20"));
			textSliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
			textSliderView.getBundle().putString("extra", itemSlider.getLink());
			textSliderView.setOnSliderClickListener(this);
			mSlider.addSlider(textSliderView);
		}

		mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
		mSlider.setCustomAnimation(new DescriptionAnimation());

	}



	private class MyRingTone extends BaseTask<String> {
		String param;

		MyRingTone(String param) {
			this.param = param;
		}

		@Override
		public String call() {
			return JsonUtils.getJSONString(param);
		}

		@Override
		public void onMyPostExecute(String result) {
			super.onMyPostExecute(result);
			if (null == result || result.length() == 0) {
				//	showToast(getString(R.string.nodata));
			} else {

				try {
					JSONObject mainJson = new JSONObject(result);
					JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);
					JSONObject objJson = null;
					for (int i = 0; i < jsonArray.length(); i++) {
						objJson = jsonArray.getJSONObject(i);

						String id = objJson.getString(Constant.TAG_ID);
						String name = objJson.getString(Constant.TAG_THEME_NAME);
						String image = objJson.getString(Constant.TAG_THEME_IMAGE);
						String imageThumb = objJson.getString(Constant.TAG_THEME_IMAGE_THUMB);
						String url = objJson.getString(Constant.TAG_THEME_URL);

						ItemTheme objItem = new ItemTheme(id, name, url, image, imageThumb);

						arrayOfRingtone.add(objItem);

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				RingTone();
			}
		}
	}

	//themes

	public void RingTone() {
		mThemeContainer.removeAllViews();
		int i = 0;
		do {
			if (i >= arrayOfRingtone.size()) {
				return;
			}

			View view = getLayoutInflater().inflate(R.layout.home_grid_view, null);

			final ImageView imageView = (ImageView) view.findViewById(R.id.img_subcategory);
			imageView.setId(i);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, mThemeImageSize));
			mThemeContainer.addView(view);
			itemRingtone = arrayOfRingtone.get(i);

			TextView themeName = view.findViewById(R.id.themes_name);
			themeName.setText(itemRingtone.getName());

			imageLoader.displayImage(itemRingtone.getThemeImag().replace(" ", "%20"), imageView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				}
			}, new ImageLoadingProgressListener() {
				@Override
				public void onProgressUpdate(String imageUri, View view, int current, int total) {
				}
			});

			imageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int position = 0;
					objAllBean = arrayOfRingtone.get(position);
					itemRingtone = arrayOfRingtone.get(imageView.getId());
					Constant.THEME_IDD = itemRingtone.getThemeId();

					String tid = objAllBean.getThemeId();
					String turl = itemRingtone.getThemUrl();

					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse(turl)));

				}
			});
			i++;
		} while (true);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		switch (menuItem.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				return true;

			default:
				return super.onOptionsItemSelected(menuItem);
		}
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
	@Override
	public void onResume(){
		super.onResume();
	}

	@Override
	public void onPause(){
		super.onPause();
	}


}
