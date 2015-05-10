package com.hudren.homevideo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
public class HomeActivity extends VideoActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = "HomeActivity";

    TitlesFragment fragment;

    private VideoServer server;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        PreferenceManager.setDefaultValues( this, R.xml.preferences, false );

        setContentView( R.layout.activity_home );

        if ( fragment == null )
        {
            fragment = new TitlesFragment();

            FragmentManager manager = getFragmentManager();
            manager.beginTransaction().add( R.id.container, fragment ).commit();
        }

        server = new VideoServer( this );
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
        fragment.setConnected( true );

        setTitle( name );
        setTitles( json );

        server.checkUpdate();
    }

    protected Fragment getVideoFragment()
    {
        return fragment;
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

        List<Title> titles = gson.fromJson( json, collectionType );
        for ( Title title : titles )
        {
            for ( Video video : title.videos )
            {
                // Sort the containers with highest priority first
                video.rankContainers();

                video.setDownloaded( downloadedContainer( video ) != null );
            }
        }

        fragment.setTitles( titles );
    }

    @Override
    protected void onStart()
    {
        super.onStart();

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

}
