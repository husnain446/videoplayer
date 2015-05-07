/*
 *
 *  * (C) Copyright 2015 byteShaft Inc.
 *  *
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser General Public License
 *  * (LGPL) version 2.1 which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl-2.1.html 
 */

package com.byteshaft.videoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.VideoView;

public class ScreenStateListener extends BroadcastReceiver {

    private VideoView mVideoView;

    public ScreenStateListener(VideoView videoView) {
        mVideoView = videoView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            }
        }
    }
}
