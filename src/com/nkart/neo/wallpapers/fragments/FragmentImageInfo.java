package com.nkart.neo.wallpapers.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.launcher3.R;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;
import com.nkart.neo.dialogs.ReportDialog;
import com.nkart.neo.wallpapers.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


@SuppressLint("SetTextI18n")
public class FragmentImageInfo extends Fragment implements View.OnClickListener {


    public static final String TAG = FragmentImageInfo.class.getSimpleName();
    private TextView imageSize, imageTitle, imageWidth, imageHeight, imageCount, imageLicense, imageReport;

    private TextView tvTitle, tvSize, tvWidth, tvHeight, tvCount, tvLicense, tvTag, tvReport;
    private Activity activity;
    private LinearLayout lTitle, lWidth, lHeight, lSize, lCount, lTag, lLicense, lReport;
    private LinearLayout llTags;

    public FragmentImageInfo() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_info, container, false);

        activity = getActivity();

        setBlurryBackground(view);
        initViews(view);
        getImageDetails();

        return view;
    }

    private void initViews(View view) {


        lTitle = (LinearLayout) view.findViewById(R.id.ll_name);
        lWidth = (LinearLayout) view.findViewById(R.id.ll_width);
        lHeight = (LinearLayout) view.findViewById(R.id.ll_height);
        lTag = (LinearLayout) view.findViewById(R.id.ll_tag);
        lLicense = (LinearLayout) view.findViewById(R.id.ll_license);
        lCount = (LinearLayout) view.findViewById(R.id.ll_count);
        lSize = (LinearLayout) view.findViewById(R.id.ll_size);
        lReport = (LinearLayout) view.findViewById(R.id.ll_report);

        tvTitle = (TextView) view.findViewById(R.id.textView_image_n);
        tvWidth = (TextView) view.findViewById(R.id.textView_image_w);
        tvHeight = (TextView) view.findViewById(R.id.textView_image_h);
        tvSize = (TextView) view.findViewById(R.id.textView_image_s);
        tvCount = (TextView) view.findViewById(R.id.textView_image_c);
        tvLicense = (TextView) view.findViewById(R.id.textView_image_l);
        tvTag = (TextView) view.findViewById(R.id.textView_image_t);
        tvReport = (TextView) view.findViewById(R.id.textView_image_r);
/*
        Extra.setTypeface(tvTitle);
        Extra.setTypeface(tvWidth);
        Extra.setTypeface(tvHeight);
        Extra.setTypeface(tvSize);
        Extra.setTypeface(tvCount);
        Extra.setTypeface(tvLicense);
        Extra.setTypeface(tvTag);
        Extra.setTypeface(tvReport);
*/
        imageSize = (TextView) view.findViewById(R.id.textView_image_size);
    //    Extra.setTypeface(imageSize);
        imageTitle = (TextView) view.findViewById(R.id.textView_image_name);
    //    Extra.setTypeface(imageTitle);
        imageWidth = (TextView) view.findViewById(R.id.textView_image_width);
    //    Extra.setTypeface(imageWidth);
        imageHeight = (TextView) view.findViewById(R.id.textView_image_height);
    //    Extra.setTypeface(imageHeight);
        imageCount = (TextView) view.findViewById(R.id.textView_image_counts);
    //    Extra.setTypeface(imageCount);
        imageLicense = (TextView) view.findViewById(R.id.textView_image_license);
    //    Extra.setTypeface(imageLicense);
        imageReport = (TextView) view.findViewById(R.id.textView_image_report);
    //    Extra.setTypeface(imageReport);
        imageReport.setOnClickListener(this);
        ImageView cross = (ImageView) view.findViewById(R.id.iv_cross);
        cross.setOnClickListener(this);


        llTags = (LinearLayout) view.findViewById(R.id.ll_tags);
    }

    private void setBlurryBackground(View view) {
        ImageView blurView = (ImageView) view.findViewById(R.id.blur_view);
        final View decorView = activity.getWindow().getDecorView();
        final View rootView = decorView.findViewById(android.R.id.content);
        blurView.setBackgroundColor(getResources().getColor(R.color.blur_bg));
    }

    private void getImageDetails() {

        try {
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        String imageID = getArguments().getString("image_id", null);

        if (imageID != null) {
            String url = getString(R.string.image_info).replace("IMAGE_ID", imageID);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        final JSONObject object = response.getJSONObject("result");

                        final JSONArray array = object.getJSONArray("tags");
                        final ArrayList<String> arrayList = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object1 = array.getJSONObject(i);
                            arrayList.add(object1.getString("name"));
                        }


                        tvTitle.setText("Title:");
                        imageTitle.setText(object.getString("name"));

                        final int[] id = {1};
                        YoYo.with(Techniques.FadeIn).duration(120).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {

                                switch (id[0]) {
                                    case 1:
                                        try {
                                            tvWidth.setText("Width:");
                                            imageWidth.setText(object.getString("height"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        YoYo.with(Techniques.FadeIn).duration(120).withListener(this).playOn(lWidth);
                                        id[0]++;
                                        break;
                                    case 2:
                                        try {
                                            tvHeight.setText("Height");
                                            imageHeight.setText(object.getString("width"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        YoYo.with(Techniques.FadeIn).duration(120).withListener(this).playOn(lHeight);
                                        id[0]++;
                                        break;
                                    case 3:
                                        try {
                                            tvSize.setText("Size:");
                                            imageSize.setText(String.format("%s KB", object.getString("filesize")));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        YoYo.with(Techniques.FadeIn).duration(120).withListener(this).playOn(lSize);
                                        id[0]++;
                                        break;

                                    case 4:
                                        try {
                                            tvCount.setText("Downloads:");
                                            imageCount.setText(object.getString("download_counter"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        YoYo.with(Techniques.FadeIn).duration(120).withListener(this).playOn(lCount);
                                        id[0]++;
                                        break;

                                    case 5:
                                        tvTag.setText("Tags:");
                                        for (String s : arrayList) {
                                            TextView tag = new TextView(getContext());
                                            tag.setBackgroundResource(R.drawable.ad_sponsor_shape);
                                            tag.setText(s);
                                            tag.setPadding(10, 10, 10, 10);
                                        //    Extra.setTypeface(tag);
                                            tag.setTextColor(Color.WHITE);
                                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            layoutParams.setMargins(0, 0, 10, 0);
                                            tag.setLayoutParams(layoutParams);
                                            llTags.addView(tag);
                                        }
                                        YoYo.with(Techniques.FadeIn).duration(120).withListener(this).playOn(lTag);
                                        id[0]++;
                                        break;

                                    case 6:
                                        tvLicense.setText("License:");
                                        imageLicense.setText("Creative Common License");
                                        YoYo.with(Techniques.FadeIn).duration(120).withListener(this).playOn(lLicense);
                                        id[0]++;
                                        break;
                                    case 7:
                                        tvReport.setText("Report image:");
                                        imageReport.setText("Click here");
                                        YoYo.with(Techniques.FadeIn).duration(120).withListener(this).playOn(lReport);
                                        id[0]++;
                                        break;
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).playOn(lTitle);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    //Toast.makeText(getContext(), getString(R.string.error_message), Toast.LENGTH_SHORT).show();

                    try {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                }
            });

            VolleySingleton.getInstance().getRequestQueue().add(jsonObjectRequest);
        } else {
            Toast.makeText(getContext(), getString(R.string.error_message), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cross:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.textView_image_report:
                ReportDialog reportDialog = new ReportDialog(getContext(), getArguments().getString("image_id", ""));
                reportDialog.show();
                break;
        }
    }

}
