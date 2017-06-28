package com.lob.music_share.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.lob.music_share.util.AndroidRecentAppsUtils;
import com.lob.music_share.util.NotificationUtils;
import com.lob.music_share.util.SpotifyUtils;
import com.lob.musicshare.R;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidRecentAppsUtils.setHeader(this);

        String MISCELLANEOUS = getResources().getString(R.string.pref_miscellaneous);
        String FEEDBACK = getResources().getString(R.string.feedback_key);

        addPreferencesFromResource(R.xml.settings);

        if (!SpotifyUtils.isSpotifyInstalled(getApplicationContext())) {
            getPreferenceScreen().removePreference(findPreference(MISCELLANEOUS));
        }

        findPreference(FEEDBACK).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "matteolob1704@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.email_body));
                startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.send_email)));
                return false;
            }
        });

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getResources().getString(R.string.notification_check_frequency))) {
            NotificationUtils.setNotificationAlarm(getApplicationContext());
        }
    }
}