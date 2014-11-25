package com.hudren.homevideo.model;

import java.io.Serializable;

/**
 * Represents a server found during discovery.
 */
public class Server implements Serializable
{
    public String name;
    public String url;

    public Server( String response )
    {
        String[] parts = response.split( "\\|" );

        url = parts[0];
        if ( parts.length > 1 )
            name = parts[1];

        if ( name != null && name.endsWith( ".local" ) )
            name = name.substring( 0, name.length() - 6 );
    }

    public String toString()
    {
        return name != null ? name : url;
    }
}
