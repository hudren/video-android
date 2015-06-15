package com.hudren.homevideo.model;

import java.io.Serializable;

/**
 * Represents information about a video file.
 */
public class Container implements Serializable
{
    public String filename;
    public String filetype;
    public String language;
    public long size;
    public long bitrate;
    public int width;
    public int height;
    public String dimension;
    public String video;
    public String audio;

    public long modified;
    public String url;
    public String mimetype;

    public String getFileSize()
    {
        return FormatUtils.sizeOf( size );
    }

    public String getBitrate()
    {
        return FormatUtils.bitrateOf( bitrate );
    }

    public boolean canDownload()
    {
        return size < 4187593114L;
    }

    public boolean hasH264()
    {
        return video != null && video.contains( "H.264" );
    }

    public boolean canCast()
    {
        return hasH264() && audio != null && audio.contains( "AAC" );
    }

    public boolean isCompatible()
    {
        return canCast() && "video/mp4".equals( mimetype );
    }

}
