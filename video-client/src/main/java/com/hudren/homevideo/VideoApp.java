package com.hudren.homevideo;

import android.app.Application;
import android.content.Context;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;

/**
 * Application that holds the VideoCastManager.
 */
public class VideoApp extends Application
{
    private static final String APPLICATION_ID = "F2714565";

    private static VideoCastManager castManager;

    /**
     * Returns the single instance of the VideoCastManager updated with the specified context.
     *
     * @param context The current context
     * @return The video cast manager instance
     */
    public static VideoCastManager getVideoCastManager( Context context )
    {
        if ( castManager == null )
        {
            castManager = VideoCastManager.initialize( context, APPLICATION_ID, null, null );
            castManager.enableFeatures( VideoCastManager.FEATURE_NOTIFICATION |
                    VideoCastManager.FEATURE_LOCKSCREEN |
                    VideoCastManager.FEATURE_WIFI_RECONNECT |
                    VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                    VideoCastManager.FEATURE_DEBUGGING );
        }

        castManager.setContext( context );

        return castManager;
    }
}
