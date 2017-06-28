/*
 * Copyright (C) 2016 Matteo Lobello
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lob.music_share.freedtouch;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

public class FreeDTouch {

    private final Handler mHandler = new Handler();

    private final View mView;
    private final OnFreeDTouchListener mOnFreeDTouchListener;

    private boolean mIsFreeDTouching;
    private boolean mIsScrolling = false;
    private boolean mForceFreeDTouch = true;

    private int mVibrationDuration = -1;
    private boolean mVibrate = true;

    private FreeDTouch(View view, OnFreeDTouchListener onFreeDTouchListener) {
        this.mView = view;
        this.mOnFreeDTouchListener = onFreeDTouchListener;
    }

    public static FreeDTouch setup(View view, OnFreeDTouchListener onFreeDTouchListener) {
        return new FreeDTouch(view, onFreeDTouchListener);
    }

    public FreeDTouch setVibration(boolean vibration) {
        this.mVibrate = vibration;
        return this;
    }

    public FreeDTouch setVibrationDuration(int duration) {
        this.mVibrationDuration = duration;
        return this;
    }

    public FreeDTouch setRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mIsScrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
                if (mIsScrolling) {
                    mOnFreeDTouchListener.onLeave();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        return this;
    }

    public void start() {
        mView.setOnTouchListener(new FreeDOnTouchListener(mOnFreeDTouchListener));
    }

    public interface OnFreeDTouchListener {
        void onFreeDTouch();

        void onLeave();
    }

    private class FreeDOnTouchListener implements View.OnTouchListener {

        private final OnFreeDTouchListener onFreeDTouchListener;

        FreeDOnTouchListener(OnFreeDTouchListener onFreeTouchListener) {
            this.onFreeDTouchListener = onFreeTouchListener;
        }

        @Override
        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                return true;
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (mIsFreeDTouching) {

                    mIsFreeDTouching = false;
                    mOnFreeDTouchListener.onLeave();
                }
                mHandler.removeCallbacksAndMessages(null);
                return true;
            }

            final float firstPressure = motionEvent.getPressure();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (firstPressure < motionEvent.getPressure()) {

                        if (mForceFreeDTouch && !mIsScrolling) {
                            if (mVibrate) {
                                ((Vibrator) mView.getContext().getSystemService(Context.VIBRATOR_SERVICE))
                                        .vibrate(mVibrationDuration == -1 ? 100 : mVibrationDuration);
                            }

                            onFreeDTouchListener.onFreeDTouch();

                            mIsFreeDTouching = true;
                        }

                        mForceFreeDTouch = false;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mForceFreeDTouch = true;
                            }
                        }, 800);

                        mHandler.removeCallbacksAndMessages(null);
                    }
                }
            }, 250);

            return true;
        }
    }
}