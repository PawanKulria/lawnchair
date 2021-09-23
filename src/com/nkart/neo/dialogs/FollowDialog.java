package com.nkart.neo.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.android.launcher3.R;
import com.nkart.neo.utils.ThemeUtils;


public class FollowDialog extends AlertDialog implements View.OnClickListener {

	private LinearLayout mFacebook, mGoogle, mYouTube, mWhatsapp;
	private Context mContext;
	private String mSharePath;

	public FollowDialog(Context context, String sharePath) {
		super(context);
		this.mContext = context;
		this.mSharePath = sharePath;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.follow_dialog);
		initView();
	}

	public void initView() {
		mFacebook = (LinearLayout) findViewById(R.id.like_facebook_layout);
		mYouTube = (LinearLayout) findViewById(R.id.like_youtube_layout);
		mGoogle = (LinearLayout) findViewById(R.id.like_googleplus_layout);
		mFacebook.setOnClickListener(this);
		mYouTube.setOnClickListener(this);
		mGoogle.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.like_facebook_layout:
				openFaceBook(mContext, mSharePath);
				this.dismiss();
				break;
			case R.id.like_youtube_layout:
				openYouTube(mContext, mSharePath);
				this.dismiss();
				break;
			case R.id.like_googleplus_layout:
				openGooglePlus(mContext, mSharePath);
				this.dismiss();
				break;
		}
	}

	private void openFaceBook(Context context, String sharePath) {

				Intent browserIntent = new Intent(Intent.ACTION_VIEW);
				browserIntent.setData(Uri.parse(ThemeUtils.FACEBOOK_URL));
				browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(browserIntent);

	}

	private void openGooglePlus(Context context, String sharePath) {

			Intent browserIntent = new Intent(Intent.ACTION_VIEW);
			browserIntent.setData(Uri.parse(ThemeUtils.GOOGLEPLUS_URL));
			browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(browserIntent);
	}



	private void openYouTube(Context context, String sharePath) {


			Intent browserIntent = new Intent(Intent.ACTION_VIEW);
			browserIntent.setData(Uri.parse(ThemeUtils.YOUTUBE_URL));
			browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(browserIntent);
	}




}
