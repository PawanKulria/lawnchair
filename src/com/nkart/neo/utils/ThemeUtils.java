package com.nkart.neo.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Iterator;
import java.util.List;


public class ThemeUtils {

	private static final boolean DEBUG = false;
	private static final String TAG = "ThemeUtil";
	public static final String FACEBOOK_URL = "https://www.facebook.com/LIALifeIsAwesome";
	public static final String GOOGLEPLUS_URL = "https://www.instagram.com/pawan_kulria/";
	public static final String YOUTUBE_URL = "https://www.youtube.com/channel/UCfCjBaIGQzrRQv2ixc7ZVtQ";
	public static final String SOLO_LAUNCHER_PACKAGENAME = "home.solo.launcher.free";
	public static final String SOLO_LAUNCHER_CLASSNAME = "home.solo.launcher.free.Launcher";
	public static final String ACTION_APPLY_SOLO_THEME = "home.solo.launcher.free.APPLY_THEME";
	private static final String EXTRA_APPLY_THEME_NAME = "EXTRA_THEMENAME";
	private static final String EXTRA_APPLY_THEME_PACKAGE = "EXTRA_PACKAGENAME";
	public static final String ACTION_INACTIVE_APPLY_THEME_FLAG = "home.solo.launcher.free.action.INACTIVE_APPLY_THEME_FLAG";
	public static final String EXTRA_INACTIVE_THEME_FLAG_PACKAGE = "EXTRA_ACTIVE_PACKAGENAME";

	public static void sendApplyThemeBroadcast(Context context, String packageName, String name) {
		Intent intent = new Intent(ThemeUtils.ACTION_APPLY_SOLO_THEME);
		intent.putExtra(EXTRA_APPLY_THEME_PACKAGE, packageName);
		intent.putExtra(EXTRA_APPLY_THEME_NAME, name);
		context.sendBroadcast(intent);
	}

	public static void inactiveApplyThemeFlag(Context context) {
		SharedPreferences preferences = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
		preferences.edit().putBoolean("apply_current_theme", false).commit();
	}

	public static void activeApplyThemeFlag(Context context) {
		if (DEBUG)
			Log.d(TAG, "activeApplyThemeFlag...package:" + context.getPackageName());

		SharedPreferences preferences = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
		preferences.edit().putBoolean("apply_current_theme", true).commit();
	}


	public static boolean isSoloLauncherRunning(Context context) {
		List<RunningAppProcessInfo> runningApps = ((ActivityManager) context.getSystemService("activity"))
				.getRunningAppProcesses();
		Iterator<RunningAppProcessInfo> iterator = runningApps.iterator();
		while (iterator.hasNext()) {
			String[] pkgList = ((RunningAppProcessInfo) iterator.next()).pkgList;
			for (String pkg : pkgList) {
				if (pkg.equals(SOLO_LAUNCHER_PACKAGENAME)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean startSoloLauncher(Context context) {
		boolean successful = false;
		try {
			context.startActivity(context.getPackageManager().getLaunchIntentForPackage(SOLO_LAUNCHER_PACKAGENAME));
			successful = true;
			return successful;
		} catch (Throwable e) {
			if (DEBUG)
				Log.e(TAG, "Start Solo Launcher error", e);
			try {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory("android.intent.category.LAUNCHER");
				intent.setComponent(new ComponentName(SOLO_LAUNCHER_PACKAGENAME, SOLO_LAUNCHER_CLASSNAME));
				context.startActivity(intent);
				successful = true;
			} catch (Throwable e2) {
				if (DEBUG)
					Log.e(TAG, "Start Solo Launcher error", e2);
			}
		}
		return successful;

	}

	public static void sendInactiveThemeFlagBroadcast(Context context) {
		if (DEBUG)
			Log.d(TAG, "sendInactiveThemeFlagBrocast...package:" + context.getPackageName());
		Intent intent = new Intent(ACTION_INACTIVE_APPLY_THEME_FLAG);
		intent.putExtra(EXTRA_INACTIVE_THEME_FLAG_PACKAGE, context.getPackageName());
		context.sendBroadcast(intent);
	}



}
