package com.hudren.homevideo.server;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by jeff on 11/17/14.
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

        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute( new HttpGet( url ) );

            int status = httpResponse.getStatusLine().getStatusCode();
            if ( status == 200 )
            {
                InputStream inputStream = httpResponse.getEntity().getContent();
                if ( inputStream != null )
                    result = convertInputStreamToString( inputStream );
            }
        }
        catch ( Exception e )
        {
            Log.e( TAG, e.getLocalizedMessage() );
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

        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpRequest = new HttpGet( request.url );
            if ( request.etag != null )
                httpRequest.addHeader( "If-None-Match", request.etag );

            HttpResponse httpResponse = httpclient.execute( httpRequest );
            response.status = httpResponse.getStatusLine().getStatusCode();

            if ( response.status == 200 )
            {
                InputStream inputStream = httpResponse.getEntity().getContent();
                if ( inputStream != null )
                    response.body = convertInputStreamToString( inputStream );
            }

            Header etagHeader = httpResponse.getFirstHeader( "Etag" );
            if ( etagHeader != null )
                response.etag = etagHeader.getValue();
        }
        catch ( Exception e )
        {
            Log.e( TAG, e.getLocalizedMessage() );
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
