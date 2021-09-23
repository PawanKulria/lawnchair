package com.nkart.neo.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.launcher3.R;

import java.util.Objects;


public class ShortcutDialog extends Dialog implements View.OnClickListener {


	private Context mContext;
	private final String TAG = ShortcutDialog.class.getSimpleName();



	public ShortcutDialog(Activity context) {
		super(context, R.style.Dialog);
		mContext = context;
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels/2;
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);

		View view = LayoutInflater.from(context).inflate(R.layout.shortcuts, null);
		setContentView(view, params);

		Objects.requireNonNull(getWindow()).setWindowAnimations(R.style.shortcut_dialog_anim);
		getWindow().setGravity(Gravity.BOTTOM);
		initView();
	}


	private void initView() {
		LinearLayout mAppManager = findViewById(R.id.app_manager_button);
		LinearLayout mBattery = findViewById(R.id.battery_button);
		LinearLayout mSounds = findViewById(R.id.sounds_button);
		LinearLayout mDisplay = findViewById(R.id.display_button);
		LinearLayout mConnections = findViewById(R.id.connections_buttons);
		LinearLayout mHideApps = findViewById(R.id.hide_apps_button);
		mAppManager.setOnClickListener(this);
		mBattery.setOnClickListener(this);
		mSounds.setOnClickListener(this);
		mDisplay.setOnClickListener(this);
		mConnections.setOnClickListener(this);
		mHideApps.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.app_manager_button:
				mContext.startActivity(new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS));
				break;
			case R.id.battery_button:
				mContext.startActivity(new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS));
				break;
			case R.id.sounds_button:
				mContext.startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
				break;
			case R.id.display_button:
				mContext.startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
				break;
			case R.id.connections_buttons:
				mContext.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
				break;
			case R.id.hide_apps_button:
				break;
		}
	}

}
