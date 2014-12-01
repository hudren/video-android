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
    public String title;
    public double duration;

    public int season;
    public int episode;
    public String episodeTitle;

    public String language;

    List< Container > containers = new ArrayList< Container >();
    public List< Subtitle > subtitles = new ArrayList< Subtitle >();

    public void rankContainers()
    {
        Collections.sort( containers, Collections.reverseOrder( new Comparator< Container >()
        {
            @Override
            public int compare( Container lhs, Container rhs )
            {
                // Most resolution
                int comp = Long.valueOf( lhs.width ).compareTo( Long.valueOf( rhs.width ) );

                // Web standards (H.264)
                if ( comp == 0 )
                    comp = Boolean.valueOf( lhs.canCast() ).compareTo( rhs.canCast() );

                // File size
                if ( comp == 0 )
                    comp = Long.valueOf( lhs.size ).compareTo( Long.valueOf( rhs.size ) );

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

    public Container getStreaming( boolean highest_quality )
    {
        if ( !highest_quality && containers.size() > 1 )
            return containers.get( 1 );

        return containers.get( 0 );
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
