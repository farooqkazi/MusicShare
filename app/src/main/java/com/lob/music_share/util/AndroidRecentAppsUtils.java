package com.lob.music_share.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.BitmapFactory;

import com.lob.musicshare.R;

public class AndroidRecentAppsUtils {
    public static void setHeader(Activity activity) {
        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(
                activity.getString(R.string.app_name), BitmapFactory.decodeResource(activity.getResources(),
                R.mipmap.ic_launcher), activity.getResources().getColor(R.color.colorPrimary));
        activity.setTaskDescription(taskDescription);
    }
}
