package com.lob.music_share.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lob.music_share.adapter.recyclerview.SongAdapter;
import com.lob.music_share.json.ParseJson;
import com.lob.music_share.query.Query;
import com.lob.music_share.user.User;
import com.lob.music_share.util.AndroidRecentAppsUtils;
import com.lob.music_share.util.Constants;
import com.lob.music_share.util.ParseValues;
import com.lob.music_share.util.PreferencesUtils;
import com.lob.music_share.util.picasso.CircleTransform;
import com.lob.music_share.util.recyclerview.RecyclerViewItemDecorator;
import com.lob.music_share.util.web.InternetUtils;
import com.lob.music_share.util.web.ServerConnectionUtils;
import com.lob.musicshare.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class OtherUserProfileActivity extends AppCompatActivity {

    private static final int[] DIALOG_TEXTVIEW_IDS = new int[]{
            R.id.other_info_artists,
            R.id.other_info_followers,
            R.id.other_info_genres
    };

    private boolean mustAddToFollowing = true;
    private boolean isSongRemovalDismissed;
    private String title, imageUrl, otherInfo, email, artists, nFollower, genres;
    private AppCompatButton followButton;
    private RecyclerView recyclerView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private TextView nameTextView, otherInfoTextView;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_other_user);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        AndroidRecentAppsUtils.setHeader(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OtherUserProfileActivity.this.supportFinishAfterTransition();
            }
        });

        title = getIntent().getExtras().getString("name-surname");
        imageUrl = getIntent().getExtras().getString("profile-image-url");
        email = getIntent().getExtras().getString("email");
        otherInfo = getIntent().getExtras().getString("other-info");

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        followButton = (AppCompatButton) findViewById(R.id.follow);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        nameTextView = (TextView) findViewById(R.id.name_other_user_text_view);
        otherInfoTextView = (TextView) findViewById(R.id.artists_other_users_text_view);

        nameTextView.setTypeface(Typeface.createFromAsset(getAssets(), Constants.ROBOTO_MEDIUM));
        nameTextView.setText(title);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        Query.getInstance(OtherUserProfileActivity.this, Query.QueryType.GET_USER_INFO)
                .setHisEmail(ParseValues.getParsedEmail(email))
                .setShowDialog(false)
                .setOnResultListener(new Query.OnResultListener() {
                    @Override
                    public void onResult(String result) {
                        User user = ParseJson.generateUsers(result).get(0);

                        artists = user.artists;
                        nFollower = String.valueOf(user.numberFollowers);
                        genres = user.genres;

                        String artists = "<b>" + getResources().getString(R.string.artists) + "</b>: " + user.artists;
                        String genres = "<b>" + getResources().getString(R.string.genres) + "</b>: " + user.genres;
                        String followers = "<b>" + getResources().getString(R.string.followers) + "</b>: " + user.numberFollowers;

                        ((TextView) findViewById(R.id.artists_other_users_text_view)).setText(Html.fromHtml(artists));
                        ((TextView) findViewById(R.id.text_view_genres)).setText(Html.fromHtml(genres));
                        ((TextView) findViewById(R.id.followers)).setText(Html.fromHtml(followers));

                        String[] parts = user.songName.split(",");
                        ArrayList<String> tracksArrayList = new ArrayList<>();
                        Collections.addAll(tracksArrayList, parts);

                        findViewById(R.id.loading_songs_frame_layout).setVisibility(View.GONE);

                        if (parts.length != 1) {
                            recyclerView.addItemDecoration(new RecyclerViewItemDecorator(getApplicationContext()));
                            recyclerView.setItemViewCacheSize(Integer.MAX_VALUE);
                            final SongAdapter adapter = new SongAdapter(OtherUserProfileActivity.this, tracksArrayList);

                            if (user.email.equals(ParseValues.getParsedEmail(PreferencesUtils.getEmail(getApplicationContext())))) {
                                ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper
                                        .SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                                    @Override
                                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                                        final int position = viewHolder.getAdapterPosition();
                                        final String titleOfSong = adapter.getTrackAt(position);
                                        final String whatToReplace = ParseValues.getParsedSongName(titleOfSong);

                                        final String message = getString(R.string.you_removed) + ": " + titleOfSong;
                                        showSnackBar(message, 3000, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                isSongRemovalDismissed = true;

                                                adapter.songs.add(position, titleOfSong);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }, whatToReplace);


                                        adapter.songs.remove(viewHolder.getAdapterPosition());
                                        adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                                    }
                                };

                                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
                                itemTouchHelper.attachToRecyclerView(recyclerView);
                            }

                            recyclerView.setAdapter(adapter);
                        } else {
                            findViewById(R.id.no_songs_frame_layout).setVisibility(View.VISIBLE);

                            String text = title + " " + OtherUserProfileActivity.this.getString(R.string.has_not_listened_yet);
                            ((TextView) OtherUserProfileActivity.this.findViewById(R.id.no_music_text_view)).setText(text);
                        }
                    }
                }).startQuery();

        loadBitmap(imageUrl);
        checkIfAlreadyFollowing();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        loadBitmap(imageUrl);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_other_user, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send_email:
                sendEmail();
                break;
            case R.id.action_other_info:
                showOtherInfoDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSnackBar(String message, int duration, View.OnClickListener onClick, final String whatToReplace) {
        final Snackbar snackbar = Snackbar.make(findViewById(R.id.recycler_view), message, Snackbar.LENGTH_INDEFINITE).setActionTextColor(Color.WHITE);

        snackbar.setAction(R.string.undo, onClick);
        snackbar.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isSongRemovalDismissed) {
                    ServerConnectionUtils.getContent(Constants.REMOVE_SONG
                            + "?email=" + ParseValues.getParsedEmail(PreferencesUtils.getEmail(getApplicationContext()))
                            + "&track=" + ParseValues.getParsedSongName(whatToReplace));
                }
                isSongRemovalDismissed = false;

                snackbar.dismiss();
            }
        }, duration);
    }

    private void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", email.replace("__at__", "@").replace("__dot__", "."), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.email_body));
        startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.send_email)));
    }

    private String addSIfPluralFollower(String value) {
        return Integer.valueOf(value) > 1 ? "s" : "";
    }

    private String addSIfPluralUsingComma(String value) {
        return value.split(",").length >= 1
                && Locale.getDefault().getLanguage().startsWith("en") ? "s" : "";
    }

    private void showOtherInfoDialog() {
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.other_info_dialog, null);

        for (int id : DIALOG_TEXTVIEW_IDS) {
            TextView textView = (TextView) dialogView.findViewById(id);
            String text = null;
            if (id == DIALOG_TEXTVIEW_IDS[0]) {
                text = textView.getText() + addSIfPluralUsingComma(artists) + ": " + artists;
            } else if (id == DIALOG_TEXTVIEW_IDS[1]) {
                text = textView.getText() + addSIfPluralFollower(nFollower) + ": " + nFollower;
            } else if (id == DIALOG_TEXTVIEW_IDS[2]) {
                text = textView.getText() + addSIfPluralUsingComma(genres) + ": " + genres;
            }
            textView.setText(text);
        }

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.other_info_about) + " " + title);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (alertDialog != null) alertDialog.dismiss();
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void checkIfAlreadyFollowing() {
        if (ParseValues.getParsedEmail(email)
                .equals(ParseValues.getParsedEmail(PreferencesUtils.getEmail(getApplicationContext())))) {
            followButton.setVisibility(View.GONE);
        } else {
            Query.getInstance(OtherUserProfileActivity.this, Query.QueryType.CHECK_IF_FOLLOWING)
                    .setHisEmail(email)
                    .setShowDialog(false)
                    .setOnResultListener(new Query.OnResultListener() {
                        @Override
                        public void onResult(String result) {
                            followButton.setBackgroundColor(getResources().getColor(R.color.md_white));
                            followButton.setVisibility(View.VISIBLE);
                            if (!result.equals("not_following")) {
                                // Already following
                                mustAddToFollowing = false;
                                setFollowButton(false);
                            } else {
                                // Not following
                                mustAddToFollowing = true;
                                setFollowButton(true);
                            }
                        }
                    }).startQuery();
        }
    }

    private void followClick() {
        if (mustAddToFollowing) {
            addToFollowing();
            setFollowButton(true);
        } else {
            removeFromFollowing();
            setFollowButton(false);
        }
        mustAddToFollowing = !mustAddToFollowing;
    }

    private void setFollowButton(boolean toFollow) {
        followButton.setText(toFollow
                ? R.string.follow
                : R.string.remove);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followClick();
            }
        });
    }

    private void removeFromFollowing() {
        Query.getInstance(OtherUserProfileActivity.this, Query.QueryType.REMOVE_FROM_FOLLOWING)
                .setHisEmail(email)
                .setOnResultListener(new Query.OnResultListener() {
                    @Override
                    public void onResult(String result) {
                        Toast.makeText(OtherUserProfileActivity.this, title + " "
                                        + OtherUserProfileActivity.this.getString(R.string.removed),
                                Toast.LENGTH_SHORT).show();
                    }
                }).startQuery();
    }

    private void addToFollowing() {
        Query.getInstance(OtherUserProfileActivity.this, Query.QueryType.ADD_TO_FOLLOWING)
                .setHisEmail(email)
                .setOnResultListener(new Query.OnResultListener() {
                    @Override
                    public void onResult(String result) {
                        ServerConnectionUtils.getContent(Constants.ADD_TO_NEW_FOLLOWERS
                                + "?myname=" + ParseValues.getParsedName(PreferencesUtils
                                .getName(OtherUserProfileActivity.this.getApplicationContext()))
                                + "&hisemail=" + email);
                    }
                }).startQuery();
        Toast.makeText(OtherUserProfileActivity.this, title + " " + getString(R.string.added), Toast.LENGTH_SHORT).show();
    }

    private void loadBitmap(final String url) {
        final ImageView imageProfile = (ImageView) findViewById(R.id.image_profile);

        if (url.equals("no")) {
            loadDefaultImage(imageProfile);
        } else {
            if (PreferencesUtils.isDataSaver(getApplicationContext())) {
                if (InternetUtils.isWiFiConnected(getApplicationContext())) {
                    loadImage(imageProfile, url);
                } else {
                    loadDefaultImage(imageProfile);
                }
            } else {
                loadImage(imageProfile, url);
            }
        }
    }

    private void loadDefaultImage(ImageView imageProfile) {
        Picasso.with(this)
                .load(R.drawable.default_image_profile)
                .transform(new CircleTransform())
                .into(imageProfile);
    }

    private void loadImage(final ImageView imageProfile, String url) {
        Picasso.with(this)
                .load(url)
                .transform(new CircleTransform())
                .into(imageProfile);
    }
}
