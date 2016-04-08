package com.hudren.homevideo.server;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP utility class.
 */
public class HttpUtil
{
    @SuppressWarnings("unused")
    private static final String TAG = "HttpUtil";

    /**
     * Returns the response from the server using the GET method.
     *
     * @param url The request url
     * @return The server response body
     */
    public static String GET( String url )
    {
        String result = "";

        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection) new URL( url ).openConnection();
            connection.setRequestProperty( "Accept-Charset", "UTF-8" );

            int status = connection.getResponseCode();
            if ( status == HttpURLConnection.HTTP_OK )
            {
                InputStream inputStream = connection.getInputStream();
                if ( inputStream != null )
                    result = convertInputStreamToString( inputStream );
            }
        }
        catch ( Exception e )
        {
            Log.e( TAG, e.getLocalizedMessage() );
        }
        finally
        {
            if ( connection != null )
                connection.disconnect();
        }

        return result;
    }

    /**
     * Returns the response from the server using the GET method.
     *
     * @param request The request
     * @return The server response
     */
    public static CachingResponse GET( CachingRequest request )
    {
        CachingResponse response = new CachingResponse();

        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection) new URL( request.url ).openConnection();
            connection.setRequestProperty( "Accept-Charset", "UTF-8" );
            if ( request.etag != null )
                connection.setRequestProperty( "If-None-Match", request.etag );

            response.status = connection.getResponseCode();
            if ( response.status == HttpURLConnection.HTTP_OK )
            {
                InputStream inputStream = connection.getInputStream();
                if ( inputStream != null )
                    response.body = convertInputStreamToString( inputStream );
            }

            response.etag = connection.getHeaderField( "Etag" );
        }
        catch ( Exception e )
        {
            Log.e( TAG, e.getLocalizedMessage() );
        }
        finally
        {
            if ( connection != null )
                connection.disconnect();
        }

        return response;
    }

    /**
     * Reads an input stream, converting it into a String.
     *
     * @param inputStream The input stream
     * @return The input stream contents
     * @throws java.io.IOException
     */
    private static String convertInputStreamToString( InputStream inputStream ) throws IOException
    {
        String result = "";

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
        try
        {
            String line = "";
            while ( (line = bufferedReader.readLine()) != null )
                result += line + "\n";
        }
        finally
        {
            bufferedReader.close();
        }

        return result;
    }

    public static class CachingRequest
    {
        public String url;
        public String etag;
    }

    public static class CachingResponse
    {
        public int status;
        public String etag;
        public String body;
    }

}
