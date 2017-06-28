package com.lob.music_share.activity;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.lob.music_share.adapter.recyclerview.SongAdapter;
import com.lob.music_share.json.ParseJson;
import com.lob.music_share.query.Query;
import com.lob.music_share.user.User;
import com.lob.music_share.util.AndroidRecentAppsUtils;
import com.lob.music_share.util.BitmapUtils;
import com.lob.music_share.util.Constants;
import com.lob.music_share.util.ParseValues;
import com.lob.music_share.util.PreferencesUtils;
import com.lob.music_share.util.picasso.CircleTransform;
import com.lob.music_share.util.recyclerview.RecyclerViewItemDecorator;
import com.lob.music_share.util.web.InternetUtils;
import com.lob.music_share.util.web.ServerConnectionUtils;
import com.lob.musicshare.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;

public class MyProfileActivity extends AppCompatActivity {

    private boolean isSongRemovalDismissed;
    private boolean isInEditMode;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        AndroidRecentAppsUtils.setHeader(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supportFinishAfterTransition();
            }
        });

        loadBitmap(getIntent().getExtras().getString("profile-image-url"));
        final String email = getIntent().getExtras().getString("email");

        (findViewById(R.id.fab))
                .setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_teal_a400)));

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        Query.getInstance(MyProfileActivity.this, Query.QueryType.GET_USER_INFO)
                .setHisEmail(ParseValues.getParsedEmail(email))
                .setShowDialog(false)
                .setOnResultListener(new Query.OnResultListener() {
                    @Override
                    public void onResult(String result) {
                        user = ParseJson.generateUsers(result).get(0);

                        String title = "<b>" + getIntent().getExtras().getString("name-surname") + "</b>";
                        String artists = "<b>" + getResources().getString(R.string.artists) + "</b>: " + user.artists;
                        String genres = "<b>" + getResources().getString(R.string.genres) + "</b>: " + user.genres;
                        String followers = "<b>" + getResources().getString(R.string.followers) + "</b>: " + user.numberFollowers;

                        ((TextView) findViewById(R.id.name_other_user_text_view)).setText(Html.fromHtml(title));
                        ((TextView) findViewById(R.id.artists_other_users_text_view)).setText(Html.fromHtml(artists));
                        ((TextView) findViewById(R.id.text_view_genres)).setText(Html.fromHtml(genres));
                        ((TextView) findViewById(R.id.followers)).setText(Html.fromHtml(followers));

                        ((EditText) findViewById(R.id.edit_artists)).setText(user.artists);
                        ((EditText) findViewById(R.id.edit_genres)).setText(user.genres);

                        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                isInEditMode = !isInEditMode;

                                if (!isInEditMode) {
                                    ((FloatingActionButton) view).setImageResource(R.drawable.edit);

                                    edit(((EditText) findViewById(R.id.edit_artists)).getText().toString(),
                                            ((EditText) findViewById(R.id.edit_genres)).getText().toString(),
                                            user);
                                } else {
                                    ((FloatingActionButton) view).setImageResource(R.drawable.tick);
                                }
                                ((ViewSwitcher) findViewById(R.id.view_switcher)).showNext();
                            }
                        });

                        String[] parts = user.songName.split(",");
                        ArrayList<String> tracksArrayList = new ArrayList<>();
                        Collections.addAll(tracksArrayList, parts);

                        findViewById(R.id.loading_songs_frame_layout).setVisibility(View.GONE);

                        if (parts.length != 1) {
                            recyclerView.addItemDecoration(new RecyclerViewItemDecorator(getApplicationContext()));
                            recyclerView.setItemViewCacheSize(Integer.MAX_VALUE);
                            final SongAdapter adapter = new SongAdapter(MyProfileActivity.this, tracksArrayList);

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

                            String text = title + " " + MyProfileActivity.this.getString(R.string.has_not_listened_yet);
                            ((TextView) MyProfileActivity.this.findViewById(R.id.no_music_text_view)).setText(text);
                        }
                    }
                }).startQuery();
    }

    private void edit(String artists, final String genres, final User user) {
        if (artists.equals(user.artists)) {
            Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
        } else {
            Query.getInstance(this, Query.QueryType.EDIT_ARTISTS)
                    .setArtist(ParseValues.getParsedArtists(artists))
                    .setShowDialog(false)
                    .setOnResultListener(new Query.OnResultListener() {
                        @Override
                        public void onResult(String result) {
                            Toast.makeText(MyProfileActivity.this, R.string.done, Toast.LENGTH_SHORT).show();
                        }
                    }).startQuery();
        }

        if (genres.equals(user.genres)) {
            Toast.makeText(MyProfileActivity.this, R.string.done, Toast.LENGTH_SHORT).show();
        } else {
            Query.getInstance(MyProfileActivity.this, Query.QueryType.EDIT_GENRES)
                    .setGenre(ParseValues.getParsedGenres(genres))
                    .setShowDialog(false)
                    .setOnResultListener(new Query.OnResultListener() {
                        @Override
                        public void onResult(String result) {
                            Toast.makeText(MyProfileActivity.this, R.string.done, Toast.LENGTH_SHORT).show();
                        }
                    }).startQuery();
        }

        reloadUser();
    }

    private void reloadUser() {
        Query.getInstance(MyProfileActivity.this, Query.QueryType.GET_USER_INFO)
                .setHisEmail(ParseValues.getParsedEmail(user.email))
                .setShowDialog(false)
                .setOnResultListener(new Query.OnResultListener() {
                    @Override
                    public void onResult(String result) {
                        user = ParseJson.generateUsers(result).get(0);

                        String artists = "<b>" + getResources().getString(R.string.artists) + "</b>: " + user.artists;
                        String genres = "<b>" + getResources().getString(R.string.genres) + "</b>: " + user.genres;

                        ((TextView) findViewById(R.id.artists_other_users_text_view)).setText(Html.fromHtml(artists));
                        ((TextView) findViewById(R.id.text_view_genres)).setText(Html.fromHtml(genres));
                    }
                }).startQuery();
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
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                        imageProfile.setImageBitmap(BitmapUtils.getCircleBitmap(bitmap));
                        imageProfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
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
}
