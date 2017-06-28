package com.lob.music_share.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.lob.music_share.adapter.recyclerview.UserAdapter;
import com.lob.music_share.json.ParseJson;
import com.lob.music_share.query.Query;
import com.lob.music_share.user.User;
import com.lob.music_share.util.AndroidRecentAppsUtils;
import com.lob.music_share.util.Constants;
import com.lob.music_share.util.NotificationUtils;
import com.lob.music_share.util.ParseValues;
import com.lob.music_share.util.PhotoSelectUtils;
import com.lob.music_share.util.PreferencesUtils;
import com.lob.music_share.util.picasso.CircleTransform;
import com.lob.music_share.util.ui.ToolbarColorizeUtils;
import com.lob.music_share.util.web.UploadProfilePicture;
import com.lob.musicshare.R;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ContentActivity extends AppCompatActivity {

    private final int SELECT_PHOTO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_content);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.showOverflowMenu();

        ((TextView) findViewById(R.id.title_toolbar))
                .setTypeface(Typeface.createFromAsset(getAssets(), Constants.ROBOTO_MEDIUM));

        ToolbarColorizeUtils.setOverflowButtonColor(this,
                new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
        setSupportActionBar(toolbar);

        loadProfilePic();

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        AndroidRecentAppsUtils.setHeader(this);

        loadPeople();
        NotificationUtils.setNotificationAlarm(getApplicationContext());
    }

    private void loadProfilePic() {
        final ImageView imageView = (ImageView) findViewById(R.id.profile_pic_toolbar);
        Query.getInstance(ContentActivity.this, Query.QueryType.GET_USER_INFO)
                .setHisEmail(ParseValues.getParsedEmail(PreferencesUtils.getEmail(getApplicationContext())))
                .setOnResultListener(new Query.OnResultListener() {
                    @Override
                    public void onResult(String result) {
                        if (result != null) {
                            final User user = ParseJson.generateUsers(result).get(0);
                            Object load = !user.profileImageUrl.equals("no")
                                    ? user.profileImageUrl
                                    : R.drawable.default_image_profile;

                            if (load instanceof String) {
                                String url = (String) load;
                                Picasso.with(getApplicationContext())
                                        .load(url)
                                        .transform(new CircleTransform())
                                        .fit()
                                        .into(imageView);
                            } else {
                                int drawable = (Integer) load;
                                Picasso.with(getApplicationContext())
                                        .load(drawable)
                                        .transform(new CircleTransform())
                                        .fit()
                                        .into(imageView);
                            }

                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(view.getContext(), MyProfileActivity.class);
                                    intent.putExtra("name-surname", user.userName);
                                    intent.putExtra("profile-image-url", user.profileImageUrl);
                                    intent.putExtra("email", user.email);
                                    intent.putExtra("other-info", user.artists);

                                    Pair<View, String> pair = Pair.create(view, "profileImage");
                                    ActivityOptionsCompat options = ActivityOptionsCompat
                                            .makeSceneTransitionAnimation(ContentActivity.this, pair);
                                    startActivity(intent, options.toBundle());
                                }
                            });
                        } else {
                            Toast.makeText(ContentActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).startQuery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                break;
            case R.id.action_change_profile_picture:
                askPermissions();
                break;
            case R.id.action_show_tutorial:
                startActivity(new Intent(ContentActivity.this, IntroductionActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(ContentActivity.this, SettingsActivity.class));
                break;
            case R.id.action_exit:
                PreferencesUtils.exit(getApplicationContext());
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void askPermissions() {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultAction() {

                    @Override
                    public void onGranted() {
                        PhotoSelectUtils.selectPhoto(ContentActivity.this, SELECT_PHOTO);
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(getApplicationContext(),
                                "Sorry, we need the Storage Permission to do that",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    Crop.of(selectedImage, Uri.fromFile(new File(Constants.PROFILE_IMAGE_PATH))).
                            asSquare().start(ContentActivity.this);
                }
                break;
            case Crop.REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    new UploadProfilePicture(ContentActivity.this,
                            ParseValues.getParsedEmail(PreferencesUtils.getEmail(getApplicationContext())),
                            Crop.getOutput(imageReturnedIntent).getPath());
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        loadPeople();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void loadPeople() {
        final ProgressWheel progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        progressWheel.setVisibility(View.VISIBLE);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setVisibility(View.GONE);

        final RelativeLayout tipContainer = (RelativeLayout) findViewById(R.id.tip_container);
        tipContainer.setVisibility(View.GONE);

        Query.getInstance(ContentActivity.this, Query.QueryType.GET_FOLLOWING)
                .setOnResultListener(new Query.OnResultListener() {
                    @Override
                    public void onResult(String result) {
                        if (!result.equals("")) {
                            progressWheel.setVisibility(View.GONE);
                            tipContainer.setVisibility(View.GONE);

                            String[] parts = result.split(ParseValues.OTHER_USER_SEPARATOR);

                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setItemViewCacheSize(Integer.MAX_VALUE);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ContentActivity.this));
                            recyclerView.setAdapter(new UserAdapter(ContentActivity.this,
                                    ((ViewGroup) findViewById(R.id.popup_container)), recyclerView, parts));
                        } else {
                            progressWheel.setVisibility(View.GONE);
                            tipContainer.setVisibility(View.VISIBLE);
                        }
                    }
                }).startQuery();
    }
}
