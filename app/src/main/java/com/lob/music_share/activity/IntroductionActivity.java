package com.lob.music_share.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lob.music_share.adapter.viewpager.IntroductionViewPagerAdapter;
import com.lob.music_share.fragment.introduction.FirstFragment;
import com.lob.music_share.fragment.introduction.SecondFragment;
import com.lob.music_share.fragment.introduction.ThirdFragment;
import com.lob.musicshare.R;

import java.util.ArrayList;

public class IntroductionActivity extends AppCompatActivity {

    private final ArrayList<Fragment> fragmentArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        fragmentArrayList.add(new FirstFragment());
        fragmentArrayList.add(new SecondFragment());
        fragmentArrayList.add(new ThirdFragment());

        final ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        pager.setOffscreenPageLimit(3);
        IntroductionViewPagerAdapter contentViewPagerAdapter = new IntroductionViewPagerAdapter(getSupportFragmentManager(),
                fragmentArrayList.toArray(new Fragment[fragmentArrayList.size()]));

        pager.setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View view, float position) {
                view.setTranslationX(view.getWidth() * -position);
                if (position <= -1.0F || position >= 1.0F) {
                    view.setAlpha(0.0F);
                } else if (position == 0.0F) {
                    view.setAlpha(1.0F);
                } else {
                    view.setAlpha(1.0F - Math.abs(position));
                }
            }
        });

        pager.setAdapter(contentViewPagerAdapter);
    }
}