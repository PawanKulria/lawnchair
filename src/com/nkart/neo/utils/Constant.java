package com.nkart.neo.utils;

import com.android.launcher3.BuildConfig;

import java.io.Serializable;

public class Constant implements Serializable {

	/**
	 * 
	 */
	//all thumb images path
	public static final String SERVER_URL = BuildConfig.SERVER_URL;

	public static final String URL_SLIDER = SERVER_URL + "api.php?slider_list";
	public static final String URL_THEME = SERVER_URL + "api.php?theme_list";

	public static final String TAG_ROOT = "THEME_APP";

	public static final String TAG_ID = "id";
	public static final String TAG_THEME_NAME = "theme_name";
	public static final String TAG_THEME_IMAGE = "theme_image";
	public static final String TAG_THEME_IMAGE_THUMB = "theme_image_thumb";
	public static final String TAG_THEME_URL = "theme_url";

	public static final String TAG_SLIDER_NAME = "slider_name";
	public static final String TAG_SLIDER_IMAGE = "slider_image";
	public static final String TAG_SLIDER_IMAGE_THUMB = "slider_image_thumb";
	public static final String TAG_SLIDER_URL = "slider_url";

	private static final long serialVersionUID = 1L;
	public static final String SERVER_IMAGE_UPFOLDER_THUMB="http://wallandthemes.com/upload/";
	public static final String SLIDER_URL = "http://wallandthemes.com/api.php?slider";
	public static final String F_THEME_URL = "http://wallandthemes.com/api.php?feature_theme";
	/*
	 * Slider
	 */
	public static final String THEMERECENT_URL = "http://wallandthemes.com/api.php?task=recent_theme";
	public static final String SLIDER_ARRAY="entertainment";
	public static final String SLIDER_NAME="name";
	public static final String SLIDER_IMAGE="image";
	public static final String SLIDER_LINK="link";
	public static final String LATEST_ARRAY_NAME="entertainment";
	public static final String THEME_ID ="id";
	public static final String THEME_URL ="theme_url";
	public static final String THEME_IMG ="theme_image";
	public static final String THEME_VIEW ="view";
	public static String THEME_IDD;

	public static String DEVICE_ID;
	public static final String RATE_MSG="view";

}
