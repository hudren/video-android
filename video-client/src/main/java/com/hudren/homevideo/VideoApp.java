package com.hudren.homevideo;

import android.app.Application;
import android.content.Context;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

/**
 * Application that holds the VideoCastManager.
 */
public class VideoApp extends Application
{
    private static final String APPLICATION_ID = "F2714565";

    /**
     * Initializes the VideoCastManager using the specified context.
     *
     * @param context The current context
     * @return The video cast manager instance
     */
    public static VideoCastManager init( Context context )
    {
        VideoCastManager castManager = VideoCastManager.initialize( context, APPLICATION_ID, null, null );

        castManager.enableFeatures( VideoCastManager.FEATURE_NOTIFICATION |
                VideoCastManager.FEATURE_LOCKSCREEN |
                VideoCastManager.FEATURE_WIFI_RECONNECT |
                VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                VideoCastManager.FEATURE_DEBUGGING );

        return castManager;
    }

}
