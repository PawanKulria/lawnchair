package com.nkart.neo.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.android.launcher3.R;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class WelcomeScreenDialog extends Dialog {


	private Context mContext;
	private Button startLauncherButton;
	public WelcomeScreenDialog(Activity context) {
		super(context, R.style.Dialog);
		mContext = context;
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		LayoutParams params = new LayoutParams(width, height);

		View view = LayoutInflater.from(context).inflate(R.layout.shuffle_dialog_app_start, null);
		setContentView(view, params);

		getWindow().setWindowAnimations(R.style.shuffle_dialog_anim);
		getWindow().setGravity(Gravity.CENTER);
		YoYo.with(Techniques.BounceIn).duration(1800).playOn(findViewById(R.id.neo_icon));
		YoYo.with(Techniques.FadeInUp).duration(2500).playOn(findViewById(R.id.neo_title));
		YoYo.with(Techniques.FadeInUp).duration(2500).playOn(findViewById(R.id.description));
		YoYo.with(Techniques.SlideInUp).duration(2500).playOn(findViewById(R.id.start_launcher_button));

		startLauncherButton = findViewById(R.id.start_launcher_button);
		startLauncherButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WelcomeScreenDialog.this.dismiss();
			//	((NexusLauncherActivity) mContext).displayInterstitialAd();
			}
		});
	}

	@Override
	public void onBackPressed(){
		WelcomeScreenDialog.this.dismiss();
	//	((NexusLauncherActivity) mContext).displayInterstitialAd();
	}
}