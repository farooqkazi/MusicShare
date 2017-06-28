package com.lob.music_share.util;

import android.app.Activity;
import android.content.Intent;

public class PhotoSelectUtils {
    public static void selectPhoto(Activity activity, int requestCode) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        activity.startActivityForResult(photoPickerIntent, requestCode);
    }
}
