package com.nkart.neo.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.android.launcher3.R;


public class ReportDialog extends Dialog implements View.OnClickListener {
	private Context mContext;
	private String mImageID;

	public ReportDialog(Context context, String imageID) {
		super(context, R.style.Dialog);
		mContext = context;
		mImageID = imageID;
		setContentView(R.layout.report_dialog);

		getWindow().setWindowAnimations(R.style.popupAnimation);
		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		TextView okBtn = (TextView) findViewById(R.id.solo_download_txv);
		okBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	//	((WallpapersActivity) mContext).reportImage(mImageID);
		this.dismiss();
	}

}
