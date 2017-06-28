package com.lob.music_share.adapter.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lob.music_share.activity.OtherUserProfileActivity;
import com.lob.music_share.freedtouch.FreeDTouch;
import com.lob.music_share.json.ParseJson;
import com.lob.music_share.query.Query;
import com.lob.music_share.user.User;
import com.lob.music_share.util.ColorPalette;
import com.lob.music_share.util.PreferencesUtils;
import com.lob.music_share.util.picasso.CircleTransform;
import com.lob.music_share.util.ui.CircularRevealUtils;
import com.lob.music_share.util.web.InternetUtils;
import com.lob.musicshare.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final Activity activity;
    private final RecyclerView recyclerView;
    private final ViewGroup popupContainer;

    private View inflatedPopup;

    private ArrayList<User> users = new ArrayList<>();
    private String[] emails;
    private int materialColorIndex = new Random().nextInt(ColorPalette.MATERIAL_COLORS.length);
    private boolean useCache;

    public UserAdapter(Activity activity, ViewGroup popupContainer, RecyclerView recyclerView, String[] emails) {
        this.emails = emails;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.popupContainer = popupContainer;
    }

    public UserAdapter(Activity activity, ViewGroup popupContainer, RecyclerView recyclerView, ArrayList<User> users) {
        this.users = users;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.popupContainer = popupContainer;
    }

    private User getUserAt(int index) {
        return users.get(index);
    }

    private void onClick(Intent intent, View profileImage) {
        Pair<View, String> pair = Pair.create(profileImage, "profileImage");
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity, pair);
        activity.startActivity(intent, options.toBundle());
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        useCache = PreferencesUtils.useCache(activity);

        return new ViewHolder(inflater.inflate(R.layout.user_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final View rootView = holder.rootView;
        final RelativeLayout cardBackground = holder.cardBackground;
        final ImageView profileImage = holder.profileImage;
        final TextView userName = holder.userName;
        final TextView moreInfo = holder.moreInfo;

        int backgroundColor;
        try {
            materialColorIndex++;
            int colorRes = ColorPalette.MATERIAL_COLORS[materialColorIndex];
            backgroundColor = activity.getResources().getColor(colorRes);
        } catch (IndexOutOfBoundsException exception) {
            materialColorIndex = 0;
            int colorRes = ColorPalette.MATERIAL_COLORS[materialColorIndex];
            backgroundColor = activity.getResources().getColor(colorRes);
        }

        cardBackground.setBackgroundColor(backgroundColor);

        CircularRevealUtils.enterReveal(rootView);

        boolean isFollowingFragment = users == null;
        if (isFollowingFragment) {
            final User user = getUserAt(position);

            final String profileImagePost = user.profileImageUrl;
            final String userNamePost = user.userName;
            final String artists = user.artists;
            final String genres = user.genres;
            final String email = user.email;

            if (!profileImagePost.equals("no")) {
                if (PreferencesUtils.isDataSaver(activity)) {
                    if (InternetUtils.isWiFiConnected(activity)) {
                        Picasso.with(rootView.getContext())
                                .load(profileImagePost + (!useCache ? "?time=" + System.currentTimeMillis() : ""))
                                .transform(new CircleTransform())
                                .fit()
                                .centerInside()
                                .into(profileImage);
                    } else {
                        Picasso.with(rootView.getContext())
                                .load(R.drawable.default_image_profile)
                                .transform(new CircleTransform())
                                .fit()
                                .centerInside()
                                .into(profileImage);
                    }
                } else {
                    Picasso.with(rootView.getContext())
                            .load(profileImagePost + (!useCache ? "?time=" + System.currentTimeMillis() : ""))
                            .transform(new CircleTransform())
                            .fit()
                            .centerInside()
                            .into(profileImage);
                }
            } else {
                Picasso.with(rootView.getContext())
                        .load(R.drawable.default_image_profile)
                        .transform(new CircleTransform())
                        .fit()
                        .centerInside()
                        .into(profileImage);
            }

            userName.setText(userNamePost);

            final String moreInfoText;
            if (!artists.equals("")) {
                moreInfoText = activity.getString(R.string.artists) + ": " + artists;
            } else if (!genres.equals("")) {
                moreInfoText = activity.getString(R.string.genres) + ": " + genres;
            } else {
                moreInfoText = activity.getString(R.string.no_more_info);
            }
            moreInfo.setText(moreInfoText);
            moreInfo.setText(moreInfoText);

            setupFreeDTouch(profileImage, user, backgroundColor);

            cardBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, OtherUserProfileActivity.class);
                    intent.putExtra("name-surname", userNamePost);
                    intent.putExtra("profile-image-url", profileImagePost);
                    intent.putExtra("email", email);
                    intent.putExtra("other-info", moreInfoText);
                    UserAdapter.this.onClick(intent, profileImage);
                }
            });
        } else {
            final int finalBackgroundColor = backgroundColor;
            Query.getInstance(activity, Query.QueryType.GET_USER_INFO)
                    .setHisEmail(emails != null ? emails[position] : getUserAt(position).email)
                    .setShowDialog(false)
                    .setOnResultListener(new Query.OnResultListener() {
                        @Override
                        public void onResult(String result) {
                            if (result != null) {
                                if (!result.equals("no_one")) {
                                    final ArrayList<User> arrayList = ParseJson.generateUsers(result);
                                    User user = arrayList.get(0);

                                    final String profileImagePost = user.profileImageUrl;
                                    final String userNamePost = user.userName;
                                    final String artists = user.artists;
                                    final String genres = user.genres;

                                    if (!profileImagePost.equals("no")) {
                                        if (!PreferencesUtils.isDataSaver(activity)) {
                                            Picasso.with(rootView.getContext())
                                                    .load(profileImagePost)
                                                    .transform(new CircleTransform())
                                                    .into(profileImage);
                                        } else {
                                            if (InternetUtils.isWiFiConnected(activity)) {
                                                Picasso.with(rootView.getContext())
                                                        .load(profileImagePost)
                                                        .transform(new CircleTransform())
                                                        .into(profileImage);
                                            } else {
                                                Picasso.with(rootView.getContext())
                                                        .load(R.drawable.default_image_profile)
                                                        .transform(new CircleTransform())
                                                        .fit()
                                                        .centerInside()
                                                        .into(profileImage);
                                            }
                                        }
                                    } else {
                                        Picasso.with(rootView.getContext())
                                                .load(R.drawable.default_image_profile)
                                                .transform(new CircleTransform())
                                                .fit()
                                                .centerInside()
                                                .into(profileImage);
                                    }

                                    userName.setText(userNamePost);
                                    userName.setTextColor(activity.getResources().getColor(R.color.md_white));

                                    Spanned moreInfoSpanned;
                                    String moreInfoString;
                                    if (!artists.equals("")) {
                                        moreInfoString = "<b>" + activity.getString(R.string.artists) + "</b>" + ": " + artists;
                                    } else if (!genres.equals("")) {
                                        moreInfoString = "<b>" + activity.getString(R.string.genres) + "</b>" + ": " + genres;
                                    } else {
                                        moreInfoString = activity.getString(R.string.no_more_info);
                                    }
                                    moreInfoString = moreInfoString.replace(": , ", ": ");

                                    moreInfoSpanned = Html.fromHtml(moreInfoString);
                                    moreInfo.setText(moreInfoSpanned);
                                    moreInfo.setTextColor(activity.getResources().getColor(R.color.md_white));

                                    setupFreeDTouch(profileImage, user, finalBackgroundColor);

                                    final String finalMoreInfoString = moreInfoString;
                                    cardBackground.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(view.getContext(), OtherUserProfileActivity.class);
                                            intent.putExtra("name-surname", userNamePost);
                                            intent.putExtra("profile-image-url", profileImagePost);
                                            intent.putExtra("email", emails != null
                                                    ? emails[position]
                                                    : UserAdapter.this.getUserAt(position).email);
                                            intent.putExtra("other-info", finalMoreInfoString);
                                            UserAdapter.this.onClick(intent, profileImage);
                                        }
                                    });
                                } else {
                                    Toast.makeText(activity, R.string.no_one, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).startQuery();
        }
    }

    private void setupFreeDTouch(final View view, final User user, final int backgroundColor) {
        FreeDTouch.setup(view, new FreeDTouch.OnFreeDTouchListener() {

            private void handleAnimation(final boolean in) {
                Animation fade = AnimationUtils.loadAnimation(activity, in
                        ? android.R.anim.fade_in
                        : android.R.anim.fade_out);
                fade.setDuration(350);
                fade.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (in) {
                            inflatedPopup.setVisibility(View.VISIBLE);
                        } else {
                            popupContainer.removeAllViews();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                if (inflatedPopup != null) {
                    inflatedPopup.startAnimation(fade);
                }
            }

            @Override
            public void onFreeDTouch() {
                inflatedPopup = LayoutInflater.from(view.getContext())
                        .inflate(R.layout.freedtouch_popup, popupContainer);
                inflatedPopup.setVisibility(View.INVISIBLE);

                CardView cardView = (CardView) inflatedPopup.findViewById(R.id.card);

                TextView name = (TextView) inflatedPopup.findViewById(R.id.text_view_user_name);
                TextView followers = (TextView) inflatedPopup.findViewById(R.id.text_view_followers);
                TextView artists = (TextView) inflatedPopup.findViewById(R.id.text_view_artists);
                TextView genres = (TextView) inflatedPopup.findViewById(R.id.text_view_genres);

                ImageView profilePic = (ImageView) inflatedPopup.findViewById(R.id.profile_image);

                cardView.setBackgroundColor(backgroundColor);

                name.setText(user.userName);
                followers.setText(activity.getResources().getString(R.string.followers) + ": " + user.numberFollowers);
                artists.setText(activity.getResources().getString(R.string.artists) + ": " + user.artists);
                genres.setText(activity.getResources().getString(R.string.genres) + ": " + user.genres);

                if (user.profileImageUrl.equals("no")) {
                    Picasso.with(activity)
                            .load(R.drawable.default_image_profile)
                            .transform(new CircleTransform())
                            .into(profilePic);
                } else {
                    Picasso.with(activity)
                            .load(user.profileImageUrl)
                            .transform(new CircleTransform())
                            .into(profilePic);
                }

                handleAnimation(true);
            }

            @Override
            public void onLeave() {
                handleAnimation(false);
            }
        }).setRecyclerView(recyclerView).start();
    }

    @Override
    public int getItemCount() {
        return emails != null ? emails.length : users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        public RelativeLayout cardBackground;
        public ImageView profileImage;
        public TextView userName;
        public TextView moreInfo;

        public ViewHolder(View rootView) {
            super(rootView);

            this.rootView = rootView;
            this.profileImage = (ImageView) rootView.findViewById(R.id.profile_image);
            this.userName = (TextView) rootView.findViewById(R.id.text_view_user_name);
            this.moreInfo = (TextView) rootView.findViewById(R.id.text_view_artists);
            this.cardBackground = (RelativeLayout) rootView.findViewById(R.id.card_background_color);
        }
    }
}