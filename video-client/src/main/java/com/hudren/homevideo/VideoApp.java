package com.hudren.homevideo;

import android.app.Application;
import android.content.Context;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.hudren.homevideo.server.VideoServer;

/**
 * Application that holds the VideoCastManager.
 */
public class VideoApp extends Application
{
    private static final String APPLICATION_ID = "F2714565";

    private static boolean initialized;

    private static boolean connected;
    private static VideoServer server;

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
            CastConfiguration options = new CastConfiguration.Builder( APPLICATION_ID )
                    .enableAutoReconnect()
                    .enableCaptionManagement()
                    .enableDebug()
                    .enableLockScreen()
                    .enableWifiReconnection()
                    .enableNotification()
                    .addNotificationAction( CastConfiguration.NOTIFICATION_ACTION_PLAY_PAUSE, true )
                    .addNotificationAction( CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, true )
                    .setNextPrevVisibilityPolicy( CastConfiguration.NEXT_PREV_VISIBILITY_POLICY_HIDDEN )
                    .setCastControllerImmersive( false )
                    .build();

            VideoCastManager.initialize( context, options );

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

    public static void setServer( VideoServer server )
    {
        VideoApp.server = server;
    }

    public static String serverUrl( String url )
    {
        String base = server.getServerUrl();
        if ( base == null )
            return url;

        if ( url != null )
        {
            // Return as is if scheme present
            if ( url.startsWith( "http" ) )
                return url;

            if ( url.startsWith( "/" ) )
                return base + url;

            return base + "/" + url;
        }

        return null;
    }

    public static ImageLoader getImageLoader()
    {
        return imageLoader;
    }
}
