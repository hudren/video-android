package com.hudren.homevideo;

import android.app.Application;
import android.content.Context;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.player.VideoCastController;

/**
 * Application that holds the VideoCastManager.
 */
public class VideoApp extends Application
{
    private static final String APPLICATION_ID = "F2714565";

    private static boolean initialized;

    private static boolean connected;

    private static ImageLoader imageLoader;

    @Override
    public void onCreate()
    {
        super.onCreate();

        imageLoader = NetworkManager.getInstance( this ).getImageLoader();
    }

    /**
     * Initializes the VideoCastManager using the specified context.
     *
     * @param context The current context
     * @return The video cast manager instance
     */
    public static synchronized VideoCastManager init( Context context )
    {
        if ( !initialized )
        {
            VideoCastManager castManager = VideoCastManager.initialize( context, APPLICATION_ID, null, null );

            castManager.enableFeatures( VideoCastManager.FEATURE_AUTO_RECONNECT |
                    VideoCastManager.FEATURE_NOTIFICATION |
                    VideoCastManager.FEATURE_LOCKSCREEN |
                    VideoCastManager.FEATURE_WIFI_RECONNECT |
                    VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                    VideoCastManager.FEATURE_DEBUGGING );

            castManager.setCastControllerImmersive( false );
            castManager.setNextPreviousVisibilityPolicy( VideoCastController.NEXT_PREV_VISIBILITY_POLICY_HIDDEN );

            initialized = true;
        }

        return VideoCastManager.getInstance();
    }

    public static boolean isConnected()
    {
        // TODO: ping server to make sure connection is still alive?
        return connected;
    }

    public static void setConnected( boolean connected )
    {
        VideoApp.connected = connected;
    }

    public static ImageLoader getImageLoader()
    {
        return imageLoader;
    }
}
