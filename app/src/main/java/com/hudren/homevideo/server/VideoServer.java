package com.hudren.homevideo.server;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.hudren.homevideo.HomeActivity;
import com.hudren.homevideo.model.Server;

/**
 * Provides server discovery and connection. This class will first attempt
 * to connect to the last known server. If not successful, it will attempt
 * to discover a server on the local network. Either way, once connected,
 * it will load the current list of available videos from the server.
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

    public VideoServer( HomeActivity activity )
    {
        this.activity = activity;

        loadPrefs();
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

        prefs.commit();
    }

    /**
     * Saves the server information for the specified
     *
     * @param url
     */
    public void saveServer( String name, String url )
    {
        network = getNetworkName();
        this.name = name;
        this.url = url;

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
        new HttpAsyncTask( name ).execute( getVideosUrl() );
    }

    /**
     * Gets a list of videos from the server
     */
    public void getVideos()
    {
        if ( isConnected() )
        {
            if ( url == null || network == null || !network.equals( getNetworkName() ) )
                discoverServer();

            else
                new HttpAsyncTask( name ).execute( getVideosUrl() );
        }
    }

    private String getVideosUrl()
    {
        return url + "/api/v1/videos";
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

    private class HttpAsyncTask extends AsyncTask< String, Void, String >
    {
        private final String name;

        private HttpAsyncTask( String name )
        {
            this.name = name;
        }

        @Override
        protected String doInBackground( String... urls )
        {
            return HttpUtil.GET( urls[0] );
        }

        @Override
        protected void onPostExecute( String result )
        {
            if ( result != null && result.length() > 0 )
            {
                activity.saveVideos( name, result );
            }
            else
            {
                discoverServer();
            }
        }
    }
}
