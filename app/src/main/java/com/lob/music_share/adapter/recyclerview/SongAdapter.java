package com.lob.music_share.adapter.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lob.music_share.query.Query;
import com.lob.music_share.util.Debug;
import com.lob.music_share.util.ParseValues;
import com.lob.music_share.util.PreferencesUtils;
import com.lob.music_share.util.SpotifyUtils;
import com.lob.music_share.util.picasso.CircleTransform;
import com.lob.music_share.util.web.InternetUtils;
import com.lob.musicshare.R;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    public final ArrayList<String> songs;
    private final Activity activity;

    public SongAdapter(Activity activity, ArrayList<String> songs) {
        this.activity = activity;

        Collections.reverse(songs);
        this.songs = songs;
    }

    public String getTrackAt(int index) {
        return songs.get(index);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ViewHolder(inflater.inflate(R.layout.song_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final View rootView = holder.rootView;
        final ImageView songThumbnail = holder.songThumbnail;
        final TextView userNameTextView = holder.userNameTextView;
        final TextView trackTextView = holder.trackTextView;

        String fullTrack = getTrackAt(position);

        if (fullTrack != null) {
            try {
                if (Character.isWhitespace(fullTrack.charAt(0))) {
                    fullTrack = fullTrack.replaceFirst("^ *", "");
                }
            } catch (StringIndexOutOfBoundsException ignored) {
            }

            final String[] songParts = fullTrack.split(" by ");
            userNameTextView.setText(songParts[0]);
            String track = getTrackAt(position);
            if (track.replace(" by ", "").equals("") || track.replace(" by ", "").equals(" ")) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) rootView.getLayoutParams();
                params.height = 0;
                rootView.setLayoutParams(params);
            } else {
                try {
                    trackTextView.setText(songParts[1]);

                    final String search = ParseValues.getParsedSongName(getTrackAt(position))
                            .replace(ParseValues.BY, "+")
                            .replace(ParseValues.TWO_DASHES, "+")
                            .replace(ParseValues.OPEN_PARENTHESIS, "")
                            .replace(ParseValues.CLOSED_PARENTHESIS, "")
                            .replace(ParseValues.OTHER_APOSTROPHE, "")
                            .replace("__opar", "")
                            .replace("__cpar", "")
                            .replace("AlbumVersion", "")
                            .replace("album version", "")
                            .replace("__dot__", "")
                            .replace("[", "")
                            .replace("]", "")
                            .replace("-", "")
                            .replace(ParseValues.COMMA, "")
                            .replace(ParseValues.ONE_BLANK_SPACE, "+")
                            .replace(ParseValues.APOSTROPHE, "")
                            .replace("c" + ParseValues.APOSTROPHE, "c'")
                            .replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "");

                    Query.getInstance(activity, Query.QueryType.ALBUM_ART)
                            .setAlbumSearch(search)
                            .setShowDialog(false)
                            .setOnResultListener(new Query.OnResultListener() {
                                @Override
                                public void onResult(String result) {
                                    if (result != null) {
                                        if (!result.equals("")) {

                                            Document document = Jsoup.parse(result);
                                            Elements elements = document.select("img");

                                            String src = "";

                                            if (elements.size() > 0) {
                                                src = elements.get(1).attr("src");
                                            }

                                            if (!PreferencesUtils.isDataSaver(activity) && !src.equals("")) {
                                                Picasso.with(activity)
                                                        .load(src)
                                                        .fit()
                                                        .into(songThumbnail);
                                            } else {
                                                if (!InternetUtils.isWiFiConnected(activity) || src.equals("")) {
                                                    Picasso.with(activity)
                                                            .load(R.drawable.no_video)
                                                            .fit()
                                                            .into(songThumbnail);
                                                } else {
                                                    Picasso.with(activity)
                                                            .load(src)
                                                            .fit()
                                                            .into(songThumbnail);
                                                }
                                            }

                                            rootView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    if (SpotifyUtils.isSpotifyInstalled(activity) && PreferencesUtils.useSpotify(activity)) {
                                                        SpotifyUtils.startSpotifySearch(activity, songParts[0] + " " + songParts[1]);
                                                    }
                                                }
                                            });
                                        } else {

                                            Picasso.with(activity)
                                                    .load(R.drawable.no_video)
                                                    .transform(new CircleTransform())
                                                    .fit()
                                                    .into(songThumbnail);

                                            rootView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    if (SpotifyUtils.isSpotifyInstalled(activity) && PreferencesUtils.useSpotify(activity)) {
                                                        SpotifyUtils.startSpotifySearch(activity, songParts[0] + " " + songParts[1]);
                                                    } else {
                                                        Toast.makeText(activity, R.string.video_not_available, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).startQuery();
                } catch (ArrayIndexOutOfBoundsException exception) {
                    Debug.log(exception);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        public ImageView songThumbnail;
        public TextView userNameTextView;
        public TextView trackTextView;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.songThumbnail = (ImageView) rootView.findViewById(R.id.songs_list_thumbnail);
            this.userNameTextView = (TextView) rootView.findViewById(R.id.songs_list_username);
            this.trackTextView = (TextView) rootView.findViewById(R.id.songs_list_track);
        }
    }
}