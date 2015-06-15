package com.hudren.homevideo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Represents a video that is displayed to the user. It may correspond to multiple containers each
 * representing a file for different encodings.
 */
public class Video implements Serializable
{
    /**
     * The bitrate used to determine unplayable videos.
     */
    public static final int UNPLAYABLE_BITRATE = 50000000;

    /**
     * The bitrate used to determine for high bandwidth videos.
     */
    public static final int HIGH_QUALITY_BITRATE = 10000000;

    public String title;
    public double duration;

    public Integer season;
    public Integer episode;
    public String episodeTitle;

    public String language;

    private boolean downloaded;

    List<Container> containers = Collections.emptyList();
    public List<Subtitle> subtitles = Collections.emptyList();

    private Integer typeRank( String mimetype )
    {
        if ( "video/mp4".equals( mimetype ) )
            return 1;

        if ( "video/x-matroska".equals( mimetype ) )
            return 2;

        return 3;
    }

    public void rankContainers()
    {
        Collections.sort( containers, Collections.reverseOrder( new Comparator<Container>()
        {
            @Override
            public int compare( Container lhs, Container rhs )
            {
                // Most resolution
                int comp = Integer.valueOf( lhs.width ).compareTo( rhs.width );

                // Container format
                if ( comp == 0 )
                    comp = typeRank( lhs.mimetype ).compareTo( typeRank( rhs.mimetype ) );

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

    public String getSubtitle( boolean more )
    {
        String text = "";

        if ( episode != null )
        {
            if ( season != null )
            {
                if ( more && episodeTitle != null )
                    text += season + "." + episode + " " + episodeTitle;
                else
                    text += "S" + season + " E" + episode;
            }
            else
            {
                if ( more && episodeTitle != null )
                    text += episode + ". " + episodeTitle;
                else
                    text += "Part " + episode;
            }
        }

        return text;
    }

    public String getFullTitle()
    {
        String text = title;

        if ( episode != null )
            text += " - " + getSubtitle( true );

        return text;
    }

    public String getDuration()
    {
        return FormatUtils.durationOf( duration );
    }

    public List<Container> getContainers()
    {
        return containers;
    }

    /**
     * Returns the best video for streaming based on quality.
     *
     * @param highest_quality True, if there are no bandwidth or performance restrictions
     * @param compatible      Prefer compatible container
     * @return The container to be used for streaming
     */
    public Container getStreaming( boolean highest_quality, boolean compatible )
    {
        long bitrate = highest_quality ? UNPLAYABLE_BITRATE : HIGH_QUALITY_BITRATE;

        if ( compatible )
        {
            // Search for MP4 container
            for ( Container container : containers )
                if ( container.bitrate <= bitrate && container.isCompatible() )
                    return container;

            // Search for H.264
            for ( Container container : containers )
                if ( container.bitrate <= bitrate && container.hasH264() )
                    return container;
        }

        // Search for anything within bitrate restriction
        for ( Container container : containers )
            if ( container.bitrate <= bitrate )
                return container;

        return null;
    }

    /**
     * Returns the best video for streaming based on bitrate and display width.
     *
     * @param width      The maximum display width in pixels]
     * @param compatible Prefer compatible container
     * @return The container to be used for streaming
     */
    public Container getStreaming( int width, boolean compatible )
    {
        List<Container> containers = containers( compatible );
        Container container = containers.get( 0 );

        int i = 1;
        // Look for first playable container
        while ( i < containers.size() && (container.bitrate > HIGH_QUALITY_BITRATE) )
            container = containers.get( i++ );

        // Look for next video greater than or equal to desired width
        while ( i < containers.size() && containers.get( i ).width > width )
            container = containers.get( i++ );

        return container;
    }

    /**
     * Returns the smallest container for downloading.
     *
     * @param compatible Prefer compatible container
     * @return The container to download
     */
    public Container getDownload( boolean compatible )
    {
        List<Container> containers = containers( compatible );

        // Find first downloadable container
        int i = 0;
        while ( i < containers.size() && !containers.get( i ).canDownload() )
            i++;

        // Find smaller container w/H.264
        while ( i + 1 < containers.size() && containers.get( i + 1 ).hasH264() && containers.get( i + 1 ).size < containers.get( i ).size )
            i++;

        return i < containers.size() ? containers.get( i ) : null;
    }

    /**
     * Returns the highest quality container for casting.
     *
     * @return The container to cast
     */
    public Container getCasting()
    {
        // Prefer mp4 container
        for ( Container container : containers )
            if ( container.isCompatible() && container.bitrate < HIGH_QUALITY_BITRATE )
                return container;

        // Prefer lower bitrate for casting
        for ( Container container : containers )
            if ( container.canCast() && container.bitrate < HIGH_QUALITY_BITRATE )
                return container;

        // Try playable videos
        for ( Container container : containers )
            if ( container.canCast() && container.bitrate < UNPLAYABLE_BITRATE )
                return container;

        return null;
    }

    private List<Container> containers( boolean compatible )
    {
        List<Container> containers = new ArrayList<>();
        if ( compatible )
        {
            // Filter compatible containers
            for ( Container container : this.containers )
                if ( container.isCompatible() )
                    containers.add( container );
        }

        // Look at all containers
        if ( containers.size() == 0 )
            containers = this.containers;

        return containers;
    }

    /**
     * Sets whether this video exists in the download directory.
     *
     * @param downloaded True, if the video has been downloaded
     */
    public void setDownloaded( boolean downloaded )
    {
        this.downloaded = downloaded;
    }

    /**
     * Returns whether this video exists in the download directory.
     *
     * @return True, if the video has been downloaded
     */
    public boolean isDownloaded()
    {
        return downloaded;
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
            TreeSet<String> langs = new TreeSet<>();
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
        ArrayList<String> dimens = new ArrayList<>();
        for ( Container container : containers )
            if ( !dimens.contains( container.dimension ) )
                dimens.add( container.dimension );

        return join( ", ", dimens );
    }

    public String getVideoCodecs()
    {
        ArrayList<String> codecs = new ArrayList<>();
        for ( Container container : containers )
            if ( !codecs.contains( container.video ) )
                codecs.add( container.video );

        return join( ", ", codecs );
    }

    public String getAudioCodecs()
    {
        ArrayList<String> codecs = new ArrayList<>();
        for ( Container container : containers )
            if ( !codecs.contains( container.audio ) )
                codecs.add( container.audio );

        return join( ", ", codecs );
    }

    public String getDownloadSize( boolean compatible )
    {
        Container container = getDownload( compatible );

        return container != null ? container.getFileSize() : null;
    }

    public boolean shouldStream( boolean streamHighQuality )
    {
        return streamHighQuality || getStreaming( false, false ).bitrate < HIGH_QUALITY_BITRATE;
    }

    public boolean canDownload()
    {
        return getDownload( false ) != null;
    }

    public boolean canCast()
    {
        return getCasting() != null;
    }

    private String join( String sep, Collection<?> coll )
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
