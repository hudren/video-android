package com.hudren.homevideo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Represents a video that is displayed to the user. It may correspond to multiple
 * containers each representing a file for different encodings.
 */
public class Video implements Serializable
{
    /**
     * The bitrate used to determine for high bandwidth videos.
     */
    public static final int HIGH_QUALITY_BITRATE = 10000000;

    public String title;
    public double duration;

    public int season;
    public int episode;
    public String episodeTitle;

    public String language;
    public String poster;

    List< Container > containers = new ArrayList< Container >();
    public List< Subtitle > subtitles = new ArrayList< Subtitle >();

    public void rankContainers()
    {
        Collections.sort( containers, Collections.reverseOrder( new Comparator< Container >()
        {
            @Override
            public int compare( Container lhs, Container rhs )
            {
                // H.264 for hardware decoding
                int comp = Boolean.valueOf( lhs.hasH264() ).compareTo( rhs.hasH264() );

                // Most resolution
                if ( comp == 0 )
                    comp = Integer.valueOf( lhs.width ).compareTo( rhs.width );

                // File size
                if ( comp == 0 )
                    comp = Long.valueOf( lhs.size ).compareTo( rhs.size );

                return comp;
            }

        } ) );
    }

    public String getTitle()
    {
        return title;
    }

    public String getDuration()
    {
        return FormatUtils.durationOf( duration );
    }

    /**
     * Returns the best video for streaming based on quality.
     *
     * @param highest_quality True, if there are no bandwidth or performance restrictions
     * @return The container to be used for streaming
     */
    public Container getStreaming( boolean highest_quality )
    {
        Container container = containers.get( 0 );

        if ( !highest_quality )
        {
            int i = 1;
            while ( i < containers.size() && container.bitrate > HIGH_QUALITY_BITRATE )
                container = containers.get( i++ );
        }

        return container;
    }

    public Container getDownload()
    {
        for ( Container container : containers )
            if ( container.canDownload() )
                return container;

        return null;
    }

    public Container getCasting()
    {
        for ( Container container : containers )
            if ( container.canCast() )
                return container;

        return null;
    }

    @Override
    public String toString()
    {
        return title + " " + getDuration();
    }

    public String getLanguage()
    {
        if ( language == null )
        {
            TreeSet< String > langs = new TreeSet< String >();
            for ( Container container : containers )
            {
                if ( container.language != null )
                {
                    String[] languages = container.language.split( "," );
                    for ( String lang : languages )
                        langs.add( lang.trim() );
                }
            }

            language = "";
            for ( String lang : langs )
            {
                if ( language.length() > 0 )
                    language += ", ";

                language += lang;
            }
        }

        return language;
    }

    public long getLastModified()
    {
        long modified = containers.get( 0 ).modified;

        for ( Container container : containers )
            modified = Math.max( modified, container.modified );

        return modified;
    }


    public long getModified()
    {
        long modified = containers.get( 0 ).modified;

        for ( Container container : containers )
            modified = Math.min( modified, container.modified );

        return modified;
    }


    public String getQuality()
    {
        ArrayList< String > dimens = new ArrayList< String >();
        for ( Container container : containers )
            if ( !dimens.contains( container.dimension ) )
                dimens.add( container.dimension );

        return join( ", ", dimens );
    }

    public String getVideoCodecs()
    {
        ArrayList< String > codecs = new ArrayList< String >();
        for ( Container container : containers )
            if ( !codecs.contains( container.video ) )
                codecs.add( container.video );

        return join( ", ", codecs );
    }

    public String getAudioCodecs()
    {
        ArrayList< String > codecs = new ArrayList< String >();
        for ( Container container : containers )
            if ( !codecs.contains( container.audio ) )
                codecs.add( container.audio );

        return join( ", ", codecs );
    }

    public String getDownloadSize()
    {
        Container container = getDownload();

        return container != null ? container.getFileSize() : null;
    }

    public boolean shouldStream( boolean streamHighQuality )
    {
        return streamHighQuality || getStreaming( false ).bitrate < HIGH_QUALITY_BITRATE;
    }

    public boolean canDownload()
    {
        return getDownload() != null;
    }

    public boolean canCast()
    {
        return getCasting() != null;
    }

    private String join( String sep, Collection< ? > coll )
    {
        String text = "";

        for ( Object obj : coll )
        {
            if ( text.length() > 0 )
                text += sep;

            text += obj.toString();
        }

        return text;
    }
}
