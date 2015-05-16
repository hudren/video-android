package com.hudren.homevideo.server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hudren.homevideo.BuildConfig;
import com.hudren.homevideo.HomeActivity;
import com.hudren.homevideo.R;
import com.hudren.homevideo.model.Server;
import com.hudren.homevideo.model.Version;

/**
 * Provides server discovery and connection. This class will first attempt to connect to the last
 * known server. If not successful, it will attempt to discover a server on the local network.
 * Either way, once connected, it will load the current list of available videos from the server.
 */
public class VideoServer
{
    @SuppressWarnings("unused")
    private static final String TAG = "VideoServer";

    private final HomeActivity activity;
    private Discovery discovery;

    private String network;
    private String name;
    private String url;
    private String etag;
    private int versionCode;

    public VideoServer( HomeActivity activity )
    {
        this.activity = activity;

        loadPrefs();

        // Initialize emulator environment to work with server running on local machine
        if ( network == null )
        {
            boolean isEmulator = Build.HARDWARE.contains( "goldfish" );
            if ( isEmulator )
                saveServer( "Local Server", "http://10.0.2.2:8090" );
        }
    }

    /**
     * Loads the network and server information for the last connected server.
     */
    public void loadPrefs()
    {
        SharedPreferences prefs = activity.getSharedPreferences();

        network = prefs.getString( "NETWORK_NAME", null );
        name = prefs.getString( "SERVER_NAME", null );
        url = prefs.getString( "SERVER_URL", null );
        etag = prefs.getString( "TITLES_ETAG", null );
        versionCode = prefs.getInt( "CLIENT_VERSION", 0 );
    }

    /**
     * Saves the network and server information for quick future connections.
     */
    public void savePrefs()
    {
        SharedPreferences.Editor prefs = activity.getSharedPreferences().edit();

        prefs.putString( "NETWORK_NAME", network );
        prefs.putString( "SERVER_NAME", name );
        prefs.putString( "SERVER_URL", url );
        prefs.putString( "TITLES_ETAG", etag );
        prefs.putInt( "CLIENT_VERSION", versionCode );

        prefs.apply();
    }

    /**
     * Saves the server information for the specified
     *
     * @param name The server name
     * @param url  The server url
     */
    public void saveServer( String name, String url )
    {
        network = getNetworkName();
        this.name = name;
        this.url = url;
        etag = null;

        savePrefs();
    }

    /**
     * Saves the server for future reference and starts the task to obtain the list of videos.
     *
     * @param server The server
     */
    public void saveServer( Server server )
    {
        saveServer( server.name, server.url );

        Toast.makeText( activity, "Connecting to server " + server, Toast.LENGTH_SHORT ).show();

        Log.d( TAG, "url = " + url );
        new GetTitlesTask( name ).execute( getTitlesRequest() );
    }

    /**
     * Gets a list of videos from the server
     */
    public void getTitles()
    {
        if ( isConnected() )
        {
            if ( url == null || network == null || !network.equals( getNetworkName() ) )
                discoverServer();

            else
                new GetTitlesTask( name ).execute( getTitlesRequest() );
        }
    }

    private HttpUtil.CachingRequest getTitlesRequest()
    {
        HttpUtil.CachingRequest request = new HttpUtil.CachingRequest();
        request.url = getTitlesUrl();
        request.etag = etag;
        return request;
    }

    private String getTitlesUrl()
    {
        return url + "/api/v1/titles";
    }

    private String getUpdateUrl()
    {
        return url + "/api/v1/android";
    }

    private String getAppUrl( String filename )
    {
        return url + "/" + filename;
    }

    /**
     * Performs server discovery.
     */
    public void discoverServer()
    {
        if ( discovery == null )
            discovery = new Discovery( this, activity );

        discovery.performDiscovery();
    }

    /**
     * Queries the server to check for an update to this app.
     */
    public void checkUpdate()
    {
        new CheckUpdateTask().execute( getUpdateUrl() );
    }

    /**
     * Returns the network SSID.
     *
     * @return The SSID
     */
    private String getNetworkName()
    {
        WifiManager manager = (WifiManager) activity.getSystemService( Context.WIFI_SERVICE );
        WifiInfo connectionInfo = manager.getConnectionInfo();
        String ssid = connectionInfo.getSSID();

        if ( ssid != null && ssid.startsWith( "\"" ) && ssid.endsWith( "\"" ) )
            ssid = ssid.substring( 1, ssid.length() - 1 );

        return ssid;
    }

    /**
     * Returns whether the network if connected.
     *
     * @return True, if the network is connected
     */
    public boolean isConnected()
    {
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService( Activity.CONNECTIVITY_SERVICE );
        NetworkInfo network = manager.getActiveNetworkInfo();

        return network != null && network.isConnected();
    }

    private class GetTitlesTask extends AsyncTask<HttpUtil.CachingRequest, Void, HttpUtil.CachingResponse>
    {
        private final String name;

        private GetTitlesTask( String name )
        {
            this.name = name;
        }

        @Override
        protected HttpUtil.CachingResponse doInBackground( HttpUtil.CachingRequest... params )
        {
            return HttpUtil.GET( params[0] );
        }

        @Override
        protected void onPostExecute( HttpUtil.CachingResponse response )
        {
            if ( response.status == 200 )
            {
                if ( response.body != null && response.body.length() > 0 )
                {
                    etag = response.etag;
                    savePrefs();

                    activity.saveTitles( name, response.body );
                }
            }
            else if ( response.status == 304 )
            {
                activity.saveTitles( name, null );
            }
            else
            {
                discoverServer();
            }
        }
    }

    private class DownloadUpdate implements DialogInterface.OnClickListener
    {
        String title;
        String filename;

        public DownloadUpdate( String filename, String title )
        {
            this.title = title;
            this.filename = filename;
        }

        @Override
        public void onClick( DialogInterface dialog, int which )
        {
            activity.downloadFile( getAppUrl( filename ), title, "application/vnd.android.package-archive", Environment.DIRECTORY_DOWNLOADS, true );
        }
    }

    private class CheckUpdateTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground( String... urls )
        {
            return HttpUtil.GET( urls[0] );
        }

        @Override
        protected void onPostExecute( String json )
        {
            Gson gson = new GsonBuilder().setFieldNamingPolicy( FieldNamingPolicy.LOWER_CASE_WITH_DASHES ).create();
            Version version = gson.fromJson( json, Version.class );

            if ( !BuildConfig.DEBUG
                    && BuildConfig.APPLICATION_ID.equals( version.packageName ) // same app
                    && Build.VERSION.SDK_INT >= version.minSdkVersion           // can run on this device
                    && version.versionCode > BuildConfig.VERSION_CODE           // is a newer version
                    && version.versionCode > versionCode )                      // haven't prompted the user
            {
                String title = activity.getString( R.string.update_title, version.label );
                String msg = activity.getString( R.string.update_msg, version.versionName );

                new AlertDialog.Builder( activity )
                        .setTitle( title )
                        .setMessage( msg )
                        .setPositiveButton( R.string.download, new DownloadUpdate( version.filename, version.label ) )
                        .setNegativeButton( R.string.no_download, null )
                        .create()
                        .show();

                // Record version to only prompt once
                versionCode = version.versionCode;
                savePrefs();
            }
        }
    }
}
