package com.lob.music_share.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;

import com.lob.music_share.util.Constants;
import com.lob.music_share.util.Debug;
import com.lob.music_share.util.ParseValues;
import com.lob.music_share.util.PreferencesUtils;
import com.lob.music_share.util.web.InternetUtils;
import com.lob.music_share.util.web.ServerConnectionUtils;

public class MusicBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (PreferencesUtils.isLoggedIn(context)) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                if (InternetUtils.hasActiveInternetConnection(context)) {
                    String email = PreferencesUtils.getEmail(context);
                    String password = PreferencesUtils.getPassword(context);
                    String artist = intent.getStringExtra("artist");
                    String track = intent.getStringExtra("track");

                    Debug.log("Music Receiver -> " + artist + ": " + track);

                    try {
                        ServerConnectionUtils.getContent(Constants.ADD_TO_LISTENED_URL
                                + "?email=" + ParseValues.getParsedEmail(email)
                                + "&pwd=" + ParseValues.getParsedPassword(password)
                                + "&artist=" + ParseValues.getParsedArtists(artist)
                                + "&track=" + ParseValues.getParsedSongName(track));
                    } catch (Exception ignored) {
                    }
                }
            } else {
                Debug.log("not logged in!");
            }
        } catch (Exception ignored) {
        }
    }
}
