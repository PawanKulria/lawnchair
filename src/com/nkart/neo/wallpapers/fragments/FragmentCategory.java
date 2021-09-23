package com.nkart.neo.wallpapers.fragments;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nkart.neo.extra.Extra;
import com.nkart.neo.wallpapers.AppData;
import com.nkart.neo.wallpapers.MainActivityWallpapers;
import com.nkart.neo.wallpapers.PicturesActivity;
import com.nkart.neo.wallpapers.VolleySingleton;
import com.nkart.neo.wallpapers.model.CategoryModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCategory extends Fragment {


    private static final String STRING_KEY = "key";
    private DisplayImageOptions displayImageOptions;
    private ArrayList<CategoryModel> arrayList = new ArrayList<>();
    private ImageLoader imageLoader;
    private RecyclerView recyclerView;
    private AVLoadingIndicatorView progressWheel;
    private ImageView imageView;
    private TextView textView;
    public FragmentCategory() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        int count;
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            count = 2;
        } else {
            count = 1;
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_category);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), count));

        TextView textViewNothing = (TextView) view.findViewById(R.id.noConnectionText);
        ImageView imageViewNothing = (ImageView) view.findViewById(R.id.noConnectionImage);
        Button retryButton = (Button) view.findViewById(R.id.retry);

        if (!Extra.isInternetON()) {
        imageViewNothing.setVisibility(View.VISIBLE);
        textViewNothing.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.VISIBLE);
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        getActivity().finish();
                        startActivity(new Intent(getActivity(), MainActivityWallpapers.class));
                        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        displayImageOptions = Extra.imageDisplayOptionCategory(getContext());
        progressWheel = (AVLoadingIndicatorView) getActivity().findViewById(R.id.av_loading);

        // Initialize ImageLoader with configuration.
        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(Extra.imageLoaderConfig(getContext()).build());
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            arrayList = savedInstanceState.getParcelableArrayList(STRING_KEY);
            recyclerView.setAdapter(new TanayAdapter(arrayList));
            if (!Extra.isInternetON()) {
                Extra.toast(getString(R.string.connect_to_internet));

            }

        } else {
            if (Extra.isInternetON()) {
                VolleyRequest();
            } else {
                Extra.mAlertDialogNoInternet(getContext());
                try {
                   // internetError();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void internetError() throws NullPointerException {
        progressWheel.setVisibility(View.GONE);
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.nothingImage_main);
        imageView.setVisibility(View.VISIBLE);
        TextView textView = (TextView) getActivity().findViewById(R.id.nothingText_main);
        textView.setVisibility(View.VISIBLE);
        Button button = (Button) getActivity().findViewById(R.id.nothingButton_main);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getActivity().finish();
                    startActivity(getActivity().getIntent());
                    getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STRING_KEY, arrayList);
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
        String url = getResources().getString(R.string.get_category);


        final SharedPreferences preferences = getActivity().getSharedPreferences("Service", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        final Boolean checked = sharedPreferences.getBoolean("checked", true);


        RequestQueue queue = VolleySingleton.getInstance().getRequestQueue();


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.e("TANAY", String.valueOf(response));


                try {
                    JSONObject jsonObject1 = response.getJSONObject("result");
                    JSONArray jsonArray = jsonObject1.getJSONArray("categories");

                    int number = jsonArray.length();

                    for (int i = 0; i < number; i++) {
                        CategoryModel categoryModel = new CategoryModel();
                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                        String albumId = jsonObject2.getString("id");   // album id
                        String photos = jsonObject2.getString("total_nb_images"); // no of photos
                        String albumName = jsonObject2.getString("name");  // album title
                        String primaryPhoto = jsonObject2.getString("tn_url");  // primary photo url


                        if (!albumName.equals("Special")) {
                            categoryModel.setAlbumId(albumId);
                            categoryModel.setAlbumName(albumName);
                            categoryModel.setPrimaryPhoto(primaryPhoto);
                            categoryModel.setCountPhoto(photos);
                            arrayList.add(categoryModel);
                        } else {
                            AppData.specialPhotosCount = photos;
                        }

                        if (checked)  //  adding number of photos at first time only
                        {
                            editor.putInt(albumName, Integer.parseInt(photos));
                            editor.apply();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    Collections.sort(arrayList, new MyCustomComparator());
                }

                progressWheel.setVisibility(View.GONE);

                recyclerView.setAdapter(new TanayAdapter(arrayList));

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (isAdded()) {
                    try {
                        //internetError();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    Extra.toast(getString(R.string.error_message));
                    progressWheel.setVisibility(View.GONE);
                    try {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        queue.add(jsonObjectRequest);

    }

    class MyCustomComparator implements Comparator<CategoryModel> {

        @Override
        public int compare(CategoryModel lhs, CategoryModel rhs) {
            return lhs.getAlbumName().compareTo(rhs.getAlbumName());
        }
    }


    private class TanayAdapter extends RecyclerView.Adapter<TanayAdapter.MyViewHolder> {
        private ArrayList<CategoryModel> arrayList;


        TanayAdapter(ArrayList<CategoryModel> data) {
            this.arrayList = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.single_row_grid, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {

            holder.title.setText(arrayList.get(position).getAlbumName());
            holder.count.setText(arrayList.get(position).getCountPhoto());
            String url = arrayList.get(position).getPrimaryPhoto();
            url = url.replace("-th", "-xs");
            imageLoader.displayImage(url, holder.image, displayImageOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    //Bitmap resized = Bitmap.createScaledBitmap(loadedImage, 1, 1, false);
                    //int color = resized.getPixel(0, 0);
                    //                holder.title.setBackgroundColor(color);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }


        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            TextView count;
            ImageView image;

            MyViewHolder(View itemView) {
                super(itemView);

                title = (TextView) itemView.findViewById(R.id.textView);
                image = (ImageView) itemView.findViewById(R.id.imageView);
                count = (TextView) itemView.findViewById(R.id.textView2);
            //    Extra.setTypeface(title);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

                CategoryModel categoryModel = arrayList.get(getLayoutPosition());
                String url = categoryModel.getPrimaryPhoto().replace("-th", "-xs");
                Intent intent = new Intent(getActivity(), PicturesActivity.class);
                intent.putExtra("albumId", categoryModel.getAlbumId());
                intent.putExtra("albumName", categoryModel.getAlbumName());
                intent.putExtra("photos", categoryModel.getCountPhoto());
                intent.putExtra("photo_url", url);
                if (Extra.isInternetON()) {

                    ActivityOptions ao;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        ao = ActivityOptions.makeSceneTransitionAnimation(getActivity(), v.findViewById(R.id.imageView), getString(R.string.share));
                        getActivity().startActivity(intent, ao.toBundle());
                    } else {
                        getActivity().startActivity(intent);
                        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }

                } else {
                    Extra.toast(getString(R.string.connect_to_internet));
                }

            }
        }
    }
}
