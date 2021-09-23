package com.nkart.neo.wallpapers;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.ToxicBakery.viewpager.transforms.BackgroundToForegroundTransformer;
import com.android.launcher3.R;
import com.google.android.material.tabs.TabLayout;
import com.nkart.neo.extra.Extra;
import com.nkart.neo.wallpapers.fragments.FragmentCategory;
import com.nkart.neo.wallpapers.fragments.FragmentRecent;

public class MainActivityWallpapers extends AppCompatActivity {
    private String mPath;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpapers_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager_main);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        MyFragmentPagerAdapter myFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(myFragmentPagerAdapter);
        myFragmentPagerAdapter.notifyDataSetChanged();
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setOffscreenPageLimit(2);
        viewPager.setPageTransformer(true, new BackgroundToForegroundTransformer());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.hd_wallpapers);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            if (Extra.isInternetON()) {
                startActivityForResult(intent, 0);
            } else {
                Extra.toast(getString(R.string.connect_to_internet));
            }
            overridePendingTransition(0, 0);
        }

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager_main);
        if (viewPager.getCurrentItem() !=0){
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 2, false);
        } else{
            // Handle this
            super.onBackPressed();
        }

    }
    @Override
    public void onResume(){
        super.onResume();

    }

    @Override
    public void onPause(){
        super.onPause();
    }

    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    fragment = new FragmentCategory();
                    break;
                case 1:
                    fragment = new FragmentRecent();
                    break;
                }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;

            switch (position) {
                case 0:
                    title = getString(R.string.tab_categories);
                    break;
                case 1:
                    title = getString(R.string.tab_recent);
                    break;

            }
            return title;
        }
    }

}
