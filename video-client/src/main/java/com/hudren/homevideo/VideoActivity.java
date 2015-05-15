package com.hudren.homevideo;

import android.app.DownloadManager;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.images.WebImage;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;
import com.hudren.homevideo.model.Container;
import com.hudren.homevideo.model.Subtitle;
import com.hudren.homevideo.model.Title;
import com.hudren.homevideo.model.Video;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for activities that play videos.
 */
public abstract class VideoActivity extends AppCompatActivity implements IPlaybackActivity
{
    private static final String TAG = "HomeActivity";
    private static final double VOLUME_INCREMENT = 0.05;

    private static final File downloadDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_MOVIES );

    private VideoCastManager castManager;
    private MiniController miniController;
    private CastConsumer castConsumer;

    private int width;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        initCasting();

        // Get movie viewing width
        WindowManager windowManager = (WindowManager) getSystemService( Context.WINDOW_SERVICE );
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics( metrics );
        width = Math.max( metrics.widthPixels, metrics.heightPixels );
    }

    protected void initCasting()
    {
        boolean castAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable( this ) == ConnectionResult.SUCCESS;
        if ( castAvailable )
        {
            castManager = VideoApp.init( this );
            castManager.reconnectSessionIfPossible();

            miniController = (MiniController) findViewById( R.id.miniController );
            castManager.addMiniController( miniController );

            castConsumer = new CastConsumer( this, castManager );
            castManager.addVideoCastConsumer( castConsumer );
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if ( castManager != null )
        {
            castManager = VideoCastManager.getInstance();
            castManager.incrementUiCounter();

            // HACK: device availability callbacks are not reliable
            setCastAvailable( true );
        }
    }

    @Override
    protected void onPause()
    {
        // VideoApp.setConnected( false );

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
        if ( castManager != null )
            castManager.addMediaRouterButton( menu, R.id.media_route_menu_item );

        return super.onCreateOptionsMenu( menu );
    }

    public SharedPreferences getSharedPreferences()
    {
        return getSharedPreferences( "app", MODE_PRIVATE );
    }

    protected Fragment getVideoFragment()
    {
        return null;
    }

    /**
     * Update the cast player volume.
     *
     * @param event The key event
     * @return True, if the event was handled
     */
    @Override
    public boolean dispatchKeyEvent( @NonNull KeyEvent event )
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
        Fragment fragment = getVideoFragment();
        if ( fragment instanceof IVideoFragment )
            ((IVideoFragment) fragment).setCastAvailable( available );
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
     * Play or stream the specified video.
     *
     * @param title The title
     * @param video The video to play or stream
     */
    public void play( Title title, Video video )
    {
        if ( canCast() )
        {
            if ( video.canCast() )
                startCasting( title, video );
            else
                Toast.makeText( this, "Cannot cast " + video.title, Toast.LENGTH_LONG ).show();
        }
        else
            startStreaming( title, video );
    }

    /**
     * Checks the download folder to determine if the container has been downloaded.
     *
     * @param container The container to check
     * @return The file for this container, or null
     */
    public File downloadedFile( Container container )
    {
        File file = new File( downloadDir, container.filename );

        return file.exists() ? file : null;
    }

    /**
     * Checks the download folder to determine if at least one container has been downloaded.
     *
     * @param video The video to check for downloads
     * @return A downloaded container, or null
     */
    public Container downloadedContainer( Video video )
    {
        for ( Container container : video.getContainers() )
            if ( downloadedFile( container ) != null )
                return container;

        return null;
    }

    /**
     * Starts an intent to stream / view the video on the current device.
     *
     * @param video The video to stream
     */
    @Override
    public void startStreaming( Title title, Video video )
    {
        try
        {
            // Get user preference for highest quality
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
            boolean quality = prefs.getBoolean( "stream_highest_quality", false );
            boolean web = prefs.getBoolean( "stream_h264", false );

            // Find the best container
            Container container;
            if ( !VideoApp.isConnected() )
                container = downloadedContainer( video );
            else if ( quality || web )
                container = video.getStreaming( quality, web );
            else
                container = video.getStreaming( width );

            // Check to see if it has already been downloaded
            if ( container != null )
            {
                File file = downloadedFile( container );
                String url = file != null ? Uri.fromFile( file ).toString() : container.url;

                Intent shareIntent = new Intent( Intent.ACTION_VIEW );
                shareIntent.setDataAndType( Uri.parse( url ), container.mimetype );
                shareIntent.putExtra( Intent.EXTRA_TITLE, title.getFullTitle( video ) );
                startActivity( shareIntent );
            }
        }
        catch ( ActivityNotFoundException e )
        {
            Toast.makeText( this, "Compatible video player is not installed.", Toast.LENGTH_LONG ).show();
        }
    }

    /**
     * Downloads a single file using the Download Manager.
     *
     * @param url         The file url
     * @param title       The title visible in the Download Manager
     * @param mimetype    The file mimetype
     * @param destination The destination directory
     * @param visible     Notification visibility during download
     * @return The download id
     */
    public long downloadFile( String url, String title, String mimetype, String destination, boolean visible )
    {
        Uri uri = Uri.parse( url );
        DownloadManager.Request request = new DownloadManager.Request( uri );
        request.setAllowedNetworkTypes( DownloadManager.Request.NETWORK_WIFI );

        if ( visible )
            request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED );
        else
            request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION );

        List<String> segments = uri.getPathSegments();
        String filename = URLDecoder.decode( segments.get( segments.size() - 1 ) );
        request.setDestinationInExternalPublicDir( destination, filename );

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
        long id = downloadFile( container.url, video.getFullTitle(), container.mimetype, Environment.DIRECTORY_MOVIES, visible );
        new DownloadMonitor( this ).execute( id );

        // Download external subtitle files
        if ( video.subtitles != null )
            for ( Subtitle subtitle : video.subtitles )
                downloadFile( subtitle.url, video.getFullTitle() + " " + subtitle.title + " subtitles", subtitle.mimetype, Environment.DIRECTORY_MOVIES, visible );
    }

    /**
     * Starts the downloads for the downloadable videos.
     *
     * @param videos The list of videos to download
     */
    public void startDownloading( List<Video> videos )
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
    protected void launchDownloads()
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
    public void startCasting( Title title, Video video )
    {
        Container container = video.getCasting();

        MediaMetadata metadata = new MediaMetadata( MediaMetadata.MEDIA_TYPE_MOVIE );
        metadata.putString( MediaMetadata.KEY_TITLE, title.getFullTitle( video ) );

        if ( title.poster != null || title.thumb != null )
        {
            metadata.addImage( new WebImage( Uri.parse( title.thumb != null ? title.thumb : title.poster ) ) );
            metadata.addImage( new WebImage( Uri.parse( title.poster != null ? title.poster : title.thumb ) ) );
        }

        // TV series
        if ( video.episode != null )
        {
            metadata.putString( MediaMetadata.KEY_SERIES_TITLE, video.title );
            metadata.putInt( MediaMetadata.KEY_SEASON_NUMBER, video.season );
            metadata.putInt( MediaMetadata.KEY_EPISODE_NUMBER, video.episode );
        }

        // Subtitles
        long id = 1;
        ArrayList<MediaTrack> tracks = new ArrayList<>();
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
        castManager.startVideoCastControllerActivity( this, mediaInfo, (int) pos, true );
    }
}
