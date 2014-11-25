package com.hudren.homevideo;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.hudren.homevideo.model.Position;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeff on 11/17/14.
 */
public class CastConsumer extends VideoCastConsumerImpl
{
    @SuppressWarnings("unused")
    private static final String TAG = "CastConsumer";

    private final HomeActivity activity;
    private final VideoCastManager castManager;

    public CastConsumer( HomeActivity activity, VideoCastManager castManager )
    {
        this.activity = activity;
        this.castManager = castManager;
    }

    /**
     * Finds the position for the specified video title.
     *
     * @param positions The list of positions
     * @param title     The video title
     * @return The saved video position, or null if not found
     */
    private static Position find( List< Position > positions, String title )
    {
        if ( positions != null )
        {
            for ( Position position : positions )
                if ( position.title.equals( title ) )
                    return position;
        }

        return null;
    }

    @Override
    public void onApplicationConnected( ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched )
    {
        Log.d( TAG, "application connected " + wasLaunched );

        if ( wasLaunched )
            restoreVolume();
    }

    @Override
    public void onRemoteMediaPlayerStatusUpdated()
    {
        Log.d( TAG, "remote media player status updated " );

        saveMediaStatus();
    }

    @Override
    public void onVolumeChanged( double value, boolean isMute )
    {
        saveVolume();
    }

    /**
     * Reads the video positions from storage.
     *
     * @return The list of saved positions
     */
    private List< Position > getVideoPositions()
    {
        SharedPreferences prefs = activity.getSharedPreferences();
        String positions = prefs.getString( "positions", "[]" );

        Gson gson = new Gson();
        Type collectionType = new TypeToken< ArrayList< Position > >()
        {
        }.getType();

        return gson.fromJson( positions, collectionType );
    }

    /**
     * Writes the video positions to storage.
     *
     * @param positions The list of positions to save
     */
    private void saveVideoPositions( List< Position > positions )
    {
        Gson gson = new Gson();
        Type collectionType = new TypeToken< ArrayList< Position > >()
        {
        }.getType();

        SharedPreferences.Editor editor = activity.getSharedPreferences().edit();
        editor.putString( "positions", gson.toJson( positions, collectionType ) );
        editor.commit();
    }

    private void saveMediaPosition( MediaInfo mediaInfo, long pos )
    {
        String title = mediaInfo.getMetadata().getString( MediaMetadata.KEY_TITLE );
        Log.d( TAG, "saving media position " + title + " " + pos );

        if ( title != null && pos > 0 )
        {
            List< Position > videoPositions = getVideoPositions();
            Position position = find( videoPositions, title );
            if ( position == null )
            {
                position = new Position( title );
                videoPositions.add( position );
            }

            position.position = pos;
            position.modified = System.currentTimeMillis();

            saveVideoPositions( videoPositions );
        }
    }

    public long getMediaPosition( MediaInfo mediaInfo )
    {
        String title = mediaInfo.getMetadata().getString( MediaMetadata.KEY_TITLE );
        Position position = find( getVideoPositions(), title );

        return position != null ? position.position : 0;
    }

    public void saveMediaStatus()
    {
        try
        {
            if ( castManager.isRemoteMediaLoaded() )
            {
                MediaInfo mediaInfo = castManager.getRemoteMediaInformation();
                long position = castManager.getCurrentMediaPosition();

                if ( mediaInfo != null && position > 0 )
                {
                    saveMediaPosition( mediaInfo, position );
                }
            }
        }
        catch ( Exception e )
        {
            Log.e( TAG, e.getLocalizedMessage(), e );
        }
    }

    /**
     * Saves the current player volume.
     */
    private void saveVolume()
    {
        try
        {
            SharedPreferences.Editor editor = activity.getSharedPreferences().edit();
            editor.putFloat( "volume", (float) castManager.getVolume() );
            editor.commit();
        }
        catch ( Exception e )
        {
            Log.e( TAG, e.getLocalizedMessage(), e );
        }
    }

    /**
     * Sets the player volume to the saved volume.
     */
    public void restoreVolume()
    {
        SharedPreferences prefs = activity.getSharedPreferences();
        float volume = prefs.getFloat( "volume", 1.0f );

        try
        {
            castManager.setVolume( volume );
        }
        catch ( Exception e )
        {
            Log.e( TAG, e.getLocalizedMessage(), e );
        }
    }
}
