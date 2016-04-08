package com.hudren.homevideo;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * The network manager provides a singleton instance for a network request queue used for loading
 * images using a simple memory-based cache.
 */
public class NetworkManager
{
    private static NetworkManager instance;

    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private NetworkManager( Context context )
    {
        requestQueue = Volley.newRequestQueue( context );

        imageLoader = new ImageLoader( requestQueue,
                new ImageLoader.ImageCache()
                {
                    private final LruCache<String, Bitmap> cache = new LruCache<>( 200 );

                    @Override
                    public Bitmap getBitmap( String url )
                    {
                        return cache.get( url );
                    }

                    @Override
                    public void putBitmap( String url, Bitmap bitmap )
                    {
                        cache.put( url, bitmap );
                    }
                } );
    }

    public static synchronized NetworkManager getInstance( Context context )
    {
        if ( instance == null )
            instance = new NetworkManager( context.getApplicationContext() );

        return instance;
    }

    public RequestQueue getRequestQueue()
    {
        return requestQueue;
    }

    public ImageLoader getImageLoader()
    {
        return imageLoader;
    }
}
