package com.hudren.homevideo.model;

import java.util.concurrent.TimeUnit;

/**
 * Utility functions for formatting numbers.
 */
public class FormatUtils
{
    public static String sizeOf( long size )
    {
        if ( size > 1073741824 )
            return String.format( "%.1f GB", (double) size / 1073741824 );
        else if ( size > 1028196 )
            return String.format( "%.0f MB", (double) size / 1028196 );
        else if ( size > 1024 )
            return String.format( "%.0f KB", (double) size / 1024 );
        else
            return String.format( "%d B", size );
    }

    public static String bitrateOf( long bitrate )
    {
        if ( bitrate > 1028196 )
            return String.format( "%.1f Mb/s", (double) bitrate / 1028196 );
        else if ( bitrate > 1024 )
            return String.format( "%.0f Kb/s", (double) bitrate / 1024 );
        else
            return String.format( "%d b/s", bitrate );
    }

    public static String durationOf( double duration )
    {
        long millis = (long) (duration * 1000);

        long hours = TimeUnit.MILLISECONDS.toHours( millis );
        long minutes = TimeUnit.MILLISECONDS.toMinutes( millis ) - TimeUnit.HOURS.toMinutes( hours );
        long seconds = TimeUnit.MILLISECONDS.toSeconds( millis ) - TimeUnit.MINUTES.toSeconds( TimeUnit.MILLISECONDS.toMinutes( millis ) );

        return String.format( "%d:%02d:%02d", hours, minutes, seconds );
    }
}
