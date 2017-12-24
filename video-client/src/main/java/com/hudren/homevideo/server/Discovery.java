package com.hudren.homevideo.server;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.hudren.homevideo.model.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Performs server discovery, returning one or more servers that respond to a broadcast event.
 */
public class Discovery
{
    @SuppressWarnings("unused")
    private static final String TAG = "Discovery";

    private static final int DISCOVERY_PORT = 8394;
    private static final int DISCOVERY_WAIT_TIME = 750;

    private final VideoServer server;
    private final Activity activity;

    public Discovery( VideoServer server, Activity activity )
    {
        this.server = server;
        this.activity = activity;
    }

    /**
     * Performs server discovery.
     */
    public void performDiscovery()
    {
        new DiscoveryTask().execute();
    }

    /**
     * Returns the address to use for local network discovery.
     *
     * @return The address to be used for broadcasting
     * @throws java.io.IOException
     */
    private InetAddress getBroadcastAddress() throws IOException
    {
        WifiManager wifi = (WifiManager) activity.getApplicationContext().getSystemService( Context.WIFI_SERVICE );
        DhcpInfo dhcp = wifi.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for ( int k = 0; k < 4; k++ )
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

        return InetAddress.getByAddress( quads );
    }

    /**
     * Sends a broadcast message for discovering servers. Will listen for a response from multiple
     * servers.
     *
     * @return A list of servers responding to the discovery broadcast
     */
    private ArrayList<Server> broadcastDiscovery()
    {
        ArrayList<Server> response = new ArrayList<>();

        String data = "DISCOVER_VIDEO_SERVER_REQUEST";
        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket();
            socket.setBroadcast( true );

            InetAddress broadcastAddress = getBroadcastAddress();
            Log.d( TAG, "sending broadcast on " + broadcastAddress.getHostAddress() );
            DatagramPacket packet = new DatagramPacket( data.getBytes(), data.length(), broadcastAddress, DISCOVERY_PORT );
            socket.send( packet );

            socket.setSoTimeout( DISCOVERY_WAIT_TIME );
            boolean done = false;
            while ( !done )
            {
                try
                {
                    Log.d( TAG, "waiting for response" );
                    byte[] buf = new byte[1024];
                    packet = new DatagramPacket( buf, buf.length );
                    socket.receive( packet );

                    String contents = new String( packet.getData() ).trim();
                    Log.d( TAG, "received " + contents );

                    response.add( new Server( packet.getAddress(), contents ) );
                }
                catch ( SocketTimeoutException e )
                {
                    done = true;
                }
            }

            Log.d( TAG, "received: " + response );

            return response;
        }
        catch ( Exception e )
        {
            Log.e( TAG, "broadcast discovery", e );
        }
        finally
        {
            if ( socket != null )
                socket.close();
        }

        return null;
    }


    /**
     * Async task to perform server discovery. Prompts the user to choose a server if more than one
     * is found.
     */
    private class DiscoveryTask extends AsyncTask<Void, Void, ArrayList<Server>>
    {

        @Override
        protected ArrayList<Server> doInBackground( Void... params )
        {
            return broadcastDiscovery();
        }

        @Override
        protected void onPostExecute( ArrayList<Server> servers )
        {
            if ( servers.size() == 1 )
            {
                Server server = servers.get( 0 );

                Discovery.this.server.saveServer( server );
            }
            else if ( servers.size() > 1 )
            {
                FragmentManager manager = activity.getFragmentManager();
                ServerDialog dialog = new ServerDialog();
                dialog.setServer( Discovery.this.server );

                Bundle bundle = new Bundle();
                bundle.putSerializable( ServerDialog.DATA, servers );
                dialog.setArguments( bundle );
                dialog.show( manager, "Dialog" );
            }
            else
            {
                Toast.makeText( activity, "Could not connect to server.", Toast.LENGTH_LONG ).show();
            }
        }
    }
}
