package com.hudren.homevideo;

import android.app.DownloadManager;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.images.WebImage;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.hudren.homevideo.model.Container;
import com.hudren.homevideo.model.Subtitle;
import com.hudren.homevideo.model.Video;
import com.hudren.homevideo.server.VideoServer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Main activity used to browse videos.
 */
public class HomeActivity extends ActionBarActivity implements IVideoActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = "HomeActivity";

    private static final double VOLUME_INCREMENT = 0.05;

    VideoFragment fragment;

    private VideoServer server;

    private VideoCastManager castManager;
    private MiniController miniController;
    private CastConsumer castConsumer;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        PreferenceManager.setDefaultValues( this, R.xml.preferences, false );

        boolean castAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable( this ) == ConnectionResult.SUCCESS;

        setContentView( R.layout.activity_home );

        if ( fragment == null )
        {
            fragment = new VideoFragment();

            FragmentManager manager = getFragmentManager();
            manager.beginTransaction().add( R.id.container, fragment ).commit();
        }

        if ( castAvailable )
        {
            castManager = VideoApp.getVideoCastManager( this );
            castManager.reconnectSessionIfPossible( this, true );

            miniController = (MiniController) findViewById( R.id.miniController1 );
            castManager.addMiniController( miniController );

            castConsumer = new CastConsumer( this, castManager );
            castManager.addVideoCastConsumer( castConsumer );
        }

        server = new VideoServer( this );
    }

    public SharedPreferences getSharedPreferences()
    {
        return getSharedPreferences( "app", MODE_PRIVATE );
    }

    /**
     * Loads the videos from storage and displays them.
     */
    public void retrieveVideos()
    {
        SharedPreferences prefs = getSharedPreferences();
        String json = prefs.getString( "videos", null );

        if ( json != null && json.length() > 0 )
            setVideos( json );
    }

    /**
     * Saves and displays the videos.
     *
     * @param name The server name
     * @param json The video json
     */
    public void saveVideos( String name, String json )
    {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString( "videos", json );
        editor.apply();

        setTitle( name );
        setVideos( json );
    }

    /**
     * Displays the specified videos.
     *
     * @param json The video json
     */
    public void setVideos( String json )
    {
        Gson gson = new GsonBuilder().setFieldNamingPolicy( FieldNamingPolicy.LOWER_CASE_WITH_DASHES ).create();
        Type collectionType = new TypeToken< ArrayList< Video > >()
        {
        }.getType();

        // Sort the containers with highest priority first
        List< Video > videos = gson.fromJson( json, collectionType );
        for ( Video video : videos )
            video.rankContainers();

        fragment.setVideos( videos );
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        retrieveVideos();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if ( castManager != null )
        {
            castManager = VideoApp.getVideoCastManager( this );
            castManager.incrementUiCounter();

            // HACK: device availability callbacks are not reliable
            setCastAvailable( true );
        }

        server.getVideos();
    }

    @Override
    protected void onPause()
    {
        if ( castManager != null )
            castManager.decrementUiCounter();

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        if ( castManager != null )
            castManager.removeMiniController( miniController );

        super.onDestroy();
    }

    /**
     * Creates the options menu, optionally adding the Cast button to the action bar.
     *
     * @param menu The menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.home, menu );

        if ( castManager != null )
            castManager.addMediaRouterButton( menu, R.id.media_route_menu_item );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        int id = item.getItemId();

        switch ( id )
        {
        case R.id.action_refresh:
            server.discoverServer();
            return true;

        case R.id.action_downloads:
            launchDownloads();
            return true;

        case R.id.action_settings:
            Intent intent = new Intent( "com.hudren.homevideo.VIEW_SETTINGS" );
            startActivityForResult( intent, 1 );
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        // Settings
        if ( requestCode == 1 )
            fragment.onPreferencesChanged();
    }

    /**
     * Update the cast player volume.
     *
     * @param event The key event
     * @return True, if the event was handled
     */
    @Override
    public boolean dispatchKeyEvent( KeyEvent event )
    {
        if ( castManager != null && castManager.onDispatchVolumeKeyEvent( event, VOLUME_INCREMENT ) )
            return true;

        return super.dispatchKeyEvent( event );
    }

    /**
     * Sets whether a device is available for casting.
     *
     * @param available True, if casting is possible
     */
    public void setCastAvailable( boolean available )
    {
        fragment.setCastAvailable( available );
    }

    /**
     * Returns whether a Google cast device is connected.
     *
     * @return True, if a video can be cast
     */
    @Override
    public boolean canCast()
    {
        return castManager != null && castManager.isConnected();
    }

    /**
     * Starts an intent to stream / view the video on the current device.
     *
     * @param video The video to stream
     */
    @Override
    public void startStreaming( Video video )
    {
        try
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
            boolean quality = prefs.getBoolean( "stream_highest_quality", true );

            Container container = video.getStreaming( quality );

            Intent shareIntent = new Intent( Intent.ACTION_VIEW );
            shareIntent.setDataAndType( Uri.parse( container.url ), container.mimetype );
            shareIntent.putExtra( Intent.EXTRA_TITLE, video.title );
            startActivity( shareIntent );
        }
        catch ( ActivityNotFoundException e )
        {
            Toast.makeText( this, "Compatible video player is not installed.", Toast.LENGTH_LONG ).show();
        }
    }

    /**
     * Downloads a single file using the Download Manager.
     *
     * @param url      The file url
     * @param title    The title visible in the Download Mangager
     * @param mimetype The file mimetype
     * @param visible  Notification visibility during download
     * @return The download id
     */
    private long downloadFile( String url, String title, String mimetype, boolean visible )
    {
        Uri uri = Uri.parse( url );
        DownloadManager.Request request = new DownloadManager.Request( uri );
        request.setAllowedNetworkTypes( DownloadManager.Request.NETWORK_WIFI );

        if ( visible )
            request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED );
        else
            request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION );

        List< String > segments = uri.getPathSegments();
        String filename = segments.get( segments.size() - 1 );
        request.setDestinationInExternalPublicDir( Environment.DIRECTORY_MOVIES, filename );

        request.setTitle( title );
        request.setMimeType( mimetype );

        Log.d( TAG, "starting download of " + url + " into " + filename );

        DownloadManager manager = (DownloadManager) getSystemService( Context.DOWNLOAD_SERVICE );
        return manager.enqueue( request );
    }

    /**
     * Queues the video with the Android Download Manager.
     *
     * @param video   The video to download
     * @param visible Notification visibility during download
     */
    public void startDownloading( Video video, boolean visible )
    {
        // Download video file
        Container container = video.getDownload();
        long id = downloadFile( container.url, video.title, container.mimetype, visible );
        new DownloadMonitor( this ).execute( id );

        // Download external subtitle files
        if ( video.subtitles != null )
            for ( Subtitle subtitle : video.subtitles )
                downloadFile( subtitle.url, video.title + " " + subtitle.title + " subtitles", subtitle.mimetype, visible );
    }

    /**
     * Starts the downloads for the downloadable videos.
     *
     * @param videos The list of videos to download
     */
    public void startDownloading( List< Video > videos )
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        boolean launch = prefs.getBoolean( "launch_downloads", false );

        // Download downloadable videos
        if ( videos != null )
            for ( Video video : videos )
                if ( video.canDownload() )
                    startDownloading( video, !launch );

        // Launch Android Downloads app
        if ( launch )
            launchDownloads();
    }

    /**
     * Launch the Android Downloads app as a new task.
     */
    private void launchDownloads()
    {
        Intent intent = new Intent( DownloadManager.ACTION_VIEW_DOWNLOADS );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( intent );
    }

    /**
     * Starts / resumes the video on a connected Google cast device.
     *
     * @param video The video to cast
     */
    @Override
    public void startCasting( Video video )
    {
        Container container = video.getCasting();

        MediaMetadata metadata = new MediaMetadata( MediaMetadata.MEDIA_TYPE_MOVIE );
        metadata.putString( MediaMetadata.KEY_TITLE, video.title );

        if ( video.poster != null )
        {
            metadata.addImage( new WebImage( Uri.parse( video.poster ) ) );
            metadata.addImage( new WebImage( Uri.parse( video.poster ) ) );
        }

        // TV series
        if ( video.episode > 0 )
        {
            metadata.putString( MediaMetadata.KEY_SERIES_TITLE, video.title );
            metadata.putInt( MediaMetadata.KEY_SEASON_NUMBER, video.season );
            metadata.putInt( MediaMetadata.KEY_EPISODE_NUMBER, video.episode );

            if ( video.episodeTitle != null )
                metadata.putString( MediaMetadata.KEY_SUBTITLE, String.format( "%d.%d %s", video.season, video.episode, video.episodeTitle ) );
            else
                metadata.putString( MediaMetadata.KEY_SUBTITLE, String.format( "Season %d - Episode %d", video.season, video.episode ) );
        }

        // Subtitles
        long id = 1;
        ArrayList< MediaTrack > tracks = new ArrayList< MediaTrack >();
        if ( video.subtitles != null )
        {
            for ( Subtitle subtitle : video.subtitles )
            {
                if ( "text/vtt".equals( subtitle.mimetype ) )
                {
                    MediaTrack track = new MediaTrack.Builder( id++, MediaTrack.TYPE_TEXT )
                            .setName( subtitle.title )
                            .setSubtype( MediaTrack.SUBTYPE_SUBTITLES )
                            .setContentId( subtitle.url )
                            .setContentType( subtitle.mimetype )
                            .setLanguage( subtitle.language )
                            .build();

                    tracks.add( track );
                }
            }
        }

        MediaInfo mediaInfo = new MediaInfo.Builder( container.url )
                .setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                .setMetadata( metadata )
                .setContentType( container.mimetype )
                .setMediaTracks( tracks )
                .build();

        // Save position of current media
        castConsumer.saveMediaStatus();

        // Start new media at saved position
        long pos = castConsumer.getMediaPosition( mediaInfo );
        castManager.startCastControllerActivity( this, mediaInfo, (int) pos, true );
    }
}
