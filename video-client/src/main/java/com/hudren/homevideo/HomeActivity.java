package com.hudren.homevideo;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hudren.homevideo.model.Title;
import com.hudren.homevideo.model.Video;
import com.hudren.homevideo.server.VideoServer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Main activity used to browse videos.
 */
public class HomeActivity
        extends VideoActivity
        implements ITitleActivity, IVideoActivity
{
    private static final String TAG = "HomeActivity";

    private VideoServer server;

    private Title title;
    private Video video;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        PreferenceManager.setDefaultValues( this, R.xml.preferences, false );

        setContentView( R.layout.activity_home );
        initCasting();

        TitleFragment titleFragment = (TitleFragment) getFragmentManager().findFragmentById( R.id.title );
        TitlesFragment titlesFragment = (TitlesFragment) getFragmentManager().findFragmentById( R.id.titles );
        titlesFragment.setMultipane( titleFragment != null );

        server = new VideoServer( this );
    }

    protected Fragment getVideoFragment()
    {
        return getFragmentManager().findFragmentById( R.id.titles );
    }

    /**
     * Loads the videos from storage and displays them.
     */
    public void retrieveTitles()
    {
        SharedPreferences prefs = getSharedPreferences();
        String json = prefs.getString( "titles", null );

        if ( json != null && json.length() > 0 )
            setTitles( json );
    }

    /**
     * Saves and displays the videos.
     *
     * @param name The server name
     * @param json The video json
     */
    public void saveTitles( String name, String json )
    {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString( "titles", json );
        editor.apply();

        VideoApp.setConnected( true );

        TitlesFragment titlesFragment = (TitlesFragment) getFragmentManager().findFragmentById( R.id.titles );
        if ( titlesFragment != null )
            titlesFragment.setConnected( true );

        if ( title == null )
        {
            TitleFragment titleFragment = (TitleFragment) getFragmentManager().findFragmentById( R.id.title );
            if ( titleFragment != null )
                titleFragment.showTitle( title );

            setTitle( name );
        }

        setTitles( json );
        invalidateOptionsMenu();

        server.checkUpdate();
    }

    /**
     * Displays the specified videos.
     *
     * @param json The video json
     */
    public void setTitles( String json )
    {
        Gson gson = new GsonBuilder().setFieldNamingPolicy( FieldNamingPolicy.LOWER_CASE_WITH_DASHES ).create();
        Type collectionType = new TypeToken<ArrayList<Title>>()
        {
        }.getType();

        try
        {
            Log.d( TAG, "parsing titles" );
            List<Title> titles = gson.fromJson( json, collectionType );

            Log.d( TAG, "ranking titles" );
            if ( titles != null )
            {
                for ( Title title : titles )
                {
                    title.rankVideos();

                    if ( title.videos != null )
                    {
                        for ( Video video : title.videos )
                        {
                            // Sort the containers with highest priority first
                            video.rankContainers();

                            video.setDownloaded( downloadedContainer( video ) != null );
                        }
                    }
                }
            }

            Log.d( TAG, "setting titles" );
            TitlesFragment titlesFragment = (TitlesFragment) getFragmentManager().findFragmentById( R.id.titles );
            titlesFragment.setTitles( titles );
        }
        catch ( Exception e )
        {
            Log.e( TAG, "error parsing json", e );
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        TitlesFragment titlesFragment = (TitlesFragment) getFragmentManager().findFragmentById( R.id.titles );
        if ( !titlesFragment.hasTitles() )
            retrieveTitles();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        server.getTitles();
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

        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onPrepareOptionsMenu( Menu menu )
    {
        MenuItem item = menu.findItem( R.id.action_play );
        if ( item != null )
            item.setVisible( title != null );

        item = menu.findItem( R.id.action_download );
        if ( item != null )
            item.setVisible( title != null );

        return super.onPrepareOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        int id = item.getItemId();

        switch ( id )
        {
        case R.id.action_play:
            if ( title != null )
                play( title, video );

            return true;

        case R.id.action_download:
            if ( title != null )
            {
                List<Video> videos = new ArrayList<>();
                videos.add( video );

                startDownloading( videos );
            }
            return true;

        case R.id.action_refresh:
            title = null;
            video = null;

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
        {
            TitlesFragment titlesFragment = (TitlesFragment) getFragmentManager().findFragmentById( R.id.titles );
            titlesFragment.onPreferencesChanged();
        }
    }

    @Override
    public Title getVideoTitle()
    {
        return title;
    }

    @Override
    public void onVideoSelected( Video video )
    {
        this.video = video;

        setTitle( title.getFullTitle( video ) );
    }

    @Override
    public void onTitleSelected( Title title )
    {
        this.title = title;
        invalidateOptionsMenu();

        if ( findViewById( R.id.title ) != null )
        {
            TitleFragment titleFragment = (TitleFragment) getFragmentManager().findFragmentById( R.id.title );
            titleFragment.showTitle( title );
        }
        else
        {
            Video video = title.getVideo();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );

            if ( video != null && (title.info == null || prefs.getBoolean( "quick_play", false )) )
                play( title, video );

            else
            {
                Intent intent = new Intent( "com.hudren.homevideo.VIEW_TITLE" );
                intent.putExtra( "title", title );
                startActivity( intent );
            }
        }
    }
}
