package com.nkart.neo.wallpapers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.android.launcher3.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nkart.neo.extra.Extra;
import com.nkart.neo.wallpapers.db.DBHelper;
import com.nkart.neo.wallpapers.model.PictureModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class SearchActivity extends AppCompatActivity {

    private EditText editText;
    private String text;
    private ArrayList<PictureModel> arrayList = new ArrayList<>();
    private AVLoadingIndicatorView progressWheel;
    private DisplayImageOptions displayImageOptions;
    private ImageLoader imageLoader;
    private static final String STRING_KEY = "key";
    private GridView gridView;
    private Button button;
    private ImageView imageView;
    private TextView textView;
    private InputMethodManager imm;
    private View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);

        progressWheel = (AVLoadingIndicatorView) findViewById(R.id.av_loading_search);
        progressWheel.setVisibility(View.GONE);
        textView = (TextView) findViewById(R.id.nothingText_search);
        imageView = (ImageView) findViewById(R.id.nothingImage_search);
        button = (Button) findViewById(R.id.nothingButton_search);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        displayImageOptions = Extra.imageDisplayOption(this);

        // Initialize ImageLoader with configuration.
        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(Extra.imageLoaderConfig(this).build());
        }


        gridView = (GridView) findViewById(R.id.gridView_search);


        editText = (EditText) findViewById(R.id.editText);
        editText.requestFocus();
        view = this.getCurrentFocus();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    editText.clearFocus();
                    if (view != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    editText.setVisibility(View.GONE);
                    getSupportActionBar().setTitle(editText.getText().toString());
                    text = editText.getText().toString();
                    if (text.equals("")) {
                        Extra.toast(getString(R.string.text_empty));
                        NothingFound();
                    } else {
                        if (Extra.isInternetON()) {
                            VolleyRequest();
                        } else {
                            Extra.toast(getString(R.string.connect_to_internet));
                            NothingFound();
                        }
                    }

                    return true;
                }
                return false;
            }
        });


        if (savedInstanceState != null) {
            arrayList = savedInstanceState.getParcelableArrayList(STRING_KEY);
            text = savedInstanceState.getString("title");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(savedInstanceState.getString("title"));
            }

            editText.setVisibility(View.GONE);
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {
            if (!Extra.isInternetON()) {
                Extra.mAlertDialogNoInternet(this);
            }
        }

        SearchAdapter searchAdapter = new SearchAdapter();
        gridView.setAdapter(searchAdapter);
        gridView.invalidate();
        searchAdapter.notifyDataSetChanged();


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STRING_KEY, arrayList);
        outState.putString("title", editText.getText().toString());
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        boolean backed = sharedPreferences.getBoolean("backPressed", false);
        if (backed) {
            gridView.setSelection(sharedPreferences.getInt("listViewPosition", 0));
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("listViewPosition", 0);
        editor.putBoolean("backPressed", false);
        editor.apply();

       /* MyApplication myApp = (MyApplication)this.getApplication();
        if (myApp.wasInBackground)
        {
            //Do specific came-here-from-background code

            ShuffleDialogOnBack shuffleDialogOnResume = new ShuffleDialogOnBack(this);
            shuffleDialogOnResume.show();
        }

        myApp.stopActivityTransitionTimer();*/
    }


    @Override
    public void onPause(){
        super.onPause();
      //  ((MyApplication)this.getApplication()).startActivityTransitionTimer();
    }

    private void VolleyRequest() {
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        final int thumbnail = sharedPreferences.getInt("thumbnail", 1);
        final int preview = sharedPreferences.getInt("preview", 1);
        final int[] length = new int[1];

        try {
            if (SearchActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                SearchActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                SearchActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        String url = getResources().getString(R.string.search_url);
        url = url.replace("SEARCH_TEXT", text);

        progressWheel.setVisibility(View.VISIBLE);

        RequestQueue queue = VolleySingleton.getInstance().getRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    JSONObject jsonObject1 = response.getJSONObject("result");
                    JSONArray jsonArray = jsonObject1.getJSONArray("images");
                    length[0] = jsonArray.length();

                    for (int i = 0; i < length[0]; i++) {

                        PictureModel pictureModel = new PictureModel(Parcel.obtain());

                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                        String urlOriginal = jsonObject2.getString("element_url");  // original url
                        pictureModel.favCount = jsonObject2.getString("hit");
                        pictureModel.pageUrl = jsonObject2.getString("page_url");

                        JSONObject jsonObject3 = jsonObject2.getJSONObject("derivatives");

                        String th_url = null;
                        String pr_url = null;

                        if (thumbnail == 0) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("small");
                            th_url = jsonObject4.getString("url");
                        } else if (thumbnail == 1) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("2small");
                            th_url = jsonObject4.getString("url");
                        } else if (thumbnail == 2) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("xsmall");
                            th_url = jsonObject4.getString("url");
                        }


                        if (preview == 0) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("small");
                            pr_url = jsonObject4.getString("url");

                        } else if (preview == 1) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("medium");
                            pr_url = jsonObject4.getString("url");
                        } else if (preview == 2) {
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("large");
                            pr_url = jsonObject4.getString("url");
                        }

                        pictureModel.url = th_url;
                        pictureModel.picMedium = pr_url;
                        pictureModel.picOriginal = urlOriginal;
                        arrayList.add(pictureModel);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    SearchActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                progressWheel.setVisibility(View.GONE);

                if (length[0] == 0) {
                    NothingFound();
                    try {
                        SearchActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }


                } else {
                    SearchAdapter adapter = new SearchAdapter();
                    gridView.setAdapter(adapter);
                    gridView.invalidate();
                    adapter.notifyDataSetChanged();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressWheel.setVisibility(View.VISIBLE);
                Extra.toast(getString(R.string.error_message));
                NothingFound();
                try {
                    SearchActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }
        });

        queue.add(jsonObjectRequest);
    }

    private void NothingFound() {
        progressWheel.setVisibility(View.GONE);
    //    Typeface typeface = Extra.getTypeFace();

        imageView.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
    //    button.setTypeface(typeface);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setVisibility(View.VISIBLE);
                editText.requestFocus();
                imm.showSoftInput(view, 1);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("");
                }
                button.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.INVISIBLE);
            }
        });
    //    textView.setTypeface(typeface);
        textView.setText(R.string.nothing_found);
        textView.setVisibility(View.VISIBLE);
    }

    class SearchAdapter extends BaseAdapter {
        PictureModel pictureModel;

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        class MyViewHolder {
            ImageView imageView;
            TextView textFav;

            MyViewHolder(View view) {
                imageView = (ImageView) view.findViewById(R.id.imageView2);
                textFav = (TextView) view.findViewById(R.id.text_fav);
            }
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            MyViewHolder myViewHolder;
            pictureModel = arrayList.get(position);
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.single_row_search, null);
                myViewHolder = new MyViewHolder(convertView);
                convertView.setTag(myViewHolder);
            } else {
                myViewHolder = (MyViewHolder) convertView.getTag();
            }

            if (DBHelper.getInstance().getFavorite(arrayList.get(position).picMedium)) {
                myViewHolder.textFav.setCompoundDrawablesWithIntrinsicBounds(ActivityCompat.getDrawable(SearchActivity.this, R.drawable.fav_x), null, null, null);
            } else {
                myViewHolder.textFav.setCompoundDrawablesWithIntrinsicBounds(ActivityCompat.getDrawable(SearchActivity.this, R.drawable.no_fav_x), null, null, null);
            }

            myViewHolder.textFav.setText(arrayList.get(position).favCount);
        //    Extra.setTypeface(myViewHolder.textFav);
            myViewHolder.textFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (DBHelper.getInstance().getFavorite(arrayList.get(position).picMedium)) {
                        DBHelper.getInstance().removeFromFavorites(arrayList.get(position).picMedium);
                        PictureModel picModel = arrayList.get(position);
                        picModel.favCount = String.valueOf(Integer.parseInt(picModel.favCount) - 1);
                        arrayList.set(position, picModel);
                        notifyDataSetChanged();
                    } else {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {

                                HttpURLConnection urlConnection = null;
                                try {
                                    URL url = new URL(arrayList.get(position).pageUrl);
                                    urlConnection = (HttpURLConnection) url.openConnection();
                                    urlConnection.connect();
                                    urlConnection.getResponseCode();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (urlConnection != null) {
                                        urlConnection.disconnect();
                                    }
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                DBHelper.getInstance().addToFavorites(arrayList.get(position).picMedium, arrayList.get(position).picOriginal);
                                PictureModel picModel = arrayList.get(position);
                                picModel.favCount = String.valueOf(Integer.parseInt(picModel.favCount) + 1);
                                arrayList.set(position, picModel);
                                notifyDataSetChanged();
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }

                }
            });

            imageLoader.displayImage(pictureModel.url, myViewHolder.imageView, displayImageOptions);

            myViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SearchActivity.this, WallpapersActivity.class);
                    intent.putExtra("position", position);
                    intent.putExtra("title", text);
                    intent.putParcelableArrayListExtra("arrayList", arrayList);
                    if (Extra.isInternetON()) {
                        startActivity(intent);
                    } else {
                        Extra.toast(getString(R.string.connect_to_internet));
                    }
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            });

            return convertView;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

}
