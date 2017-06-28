package com.lob.music_share.util.ui.button;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.lob.music_share.util.Constants;

public class IntroductionButton extends android.support.v7.widget.AppCompatButton {
    public IntroductionButton(Context context) {
        super(context);
        init();
    }

    public IntroductionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IntroductionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), Constants.TYPEFACE_NAME_TITLES));
        setTextColor(Color.BLACK);
    }
}
