package com.lob.music_share.activity;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lob.music_share.adapter.viewpager.ViewPagerAdapter;
import com.lob.music_share.fragment.content.SearchFragment;
import com.lob.music_share.query.Query;
import com.lob.music_share.util.AndroidRecentAppsUtils;
import com.lob.music_share.util.ui.StatusBarColorChanger;
import com.lob.musicshare.R;

public class SearchActivity extends AppCompatActivity {

    private final Fragment[] FRAGMENTS = {
            new SearchFragment(Query.QueryType.PEOPLE),
            new SearchFragment(Query.QueryType.BY_ARTISTS),
            new SearchFragment(Query.QueryType.BY_GENRES)
    };
    private Toolbar toolbar;
    private ViewPagerAdapter viewPagerAdapter;
    private int currentFragmentIndex;
    private int[] TITLES = {
            R.string.all, R.string.artists, R.string.genres
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        getWindow().setStatusBarColor(getResources().getColor(R.color.md_grey_300));
        StatusBarColorChanger.setLightStatusBar(toolbar);

        AndroidRecentAppsUtils.setHeader(this);

        createMaterialSearchView(toolbar, getResources().getString(R.string.search));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(R.drawable.back_arrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FRAGMENTS);
        pager.setOffscreenPageLimit(3);
        pager.setAdapter(viewPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabTextColors(Color.parseColor("#4a4a4a"), Color.parseColor("#4a4a4a"));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimary));
        tabLayout.setTabTextColors(Color.parseColor("#4a4a4a"), getResources().getColor(R.color.colorPrimary));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFragmentIndex = tab.getPosition();
                pager.setCurrentItem(currentFragmentIndex);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        for (int i = 0; i < TITLES.length; i++) {
            tabLayout.getTabAt(i).setText(TITLES[i]);
        }
    }

    private void createMaterialSearchView(Toolbar toolbar, String hintText) {
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        SearchView searchView = new SearchView(this);
        searchView.setIconifiedByDefault(false);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint(hintText);

        int rightMarginFrame = 0;
        View frame = searchView.findViewById(getResources().getIdentifier("android:id/search_edit_frame", null, null));
        if (frame != null) {
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            rightMarginFrame = ((LinearLayout.LayoutParams) frame.getLayoutParams()).rightMargin;
            frameParams.setMargins(0, 0, 0, 0);
            frame.setLayoutParams(frameParams);
        }

        View plate = searchView.findViewById(getResources().getIdentifier("android:id/search_plate", null, null));
        if (plate != null) {
            plate.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            plate.setPadding(0, 0, rightMarginFrame, 0);
            plate.setBackgroundColor(Color.TRANSPARENT);
        }

        int autoCompleteId = getResources().getIdentifier("android:id/search_src_text", null, null);
        if (searchView.findViewById(autoCompleteId) != null) {
            EditText autoComplete = (EditText) searchView.findViewById(autoCompleteId);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, (int) convertDpToPixel(36));
            params.weight = 1;
            params.gravity = Gravity.CENTER_VERTICAL;
            params.leftMargin = rightMarginFrame;
            autoComplete.setLayoutParams(params);
            autoComplete.setTextSize(16f);
        }

        int searchMagId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        if (searchView.findViewById(searchMagId) != null) {
            ImageView v = (ImageView) searchView.findViewById(searchMagId);
            v.setImageDrawable(null);
            v.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 0);
            v.setLayoutParams(params);
            v.setColorFilter(Color.BLACK);
        }

        toolbar.setTitle(null);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.addView(searchView);

        ((ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon)).setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_grey_700)));
        ((ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn)).setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_grey_700)));
        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(getResources().getColor(R.color.md_grey_700));
        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(getResources().getColor(R.color.md_grey_700));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                for (int i = 0; i < TITLES.length; i++) {
                    ((SearchFragment) viewPagerAdapter.getItem(i)).search(SearchActivity.this, query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private float convertDpToPixel(int dp) {
        Resources res = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }
}
