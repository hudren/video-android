package com.hudren.homevideo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a title that can be either a single video, a multipart movie, or a number of seasons
 * of TV shows.
 */
public class Title implements Serializable
{
    public String id;
    public String title;
    public String sorting;

    public String poster;
    public String thumb;

    public Info info;

    public List<Video> videos = Collections.emptyList();

    public String getTitle()
    {
        return title;
    }

    public String getSortingTitle()
    {
        return sorting != null ? sorting : title;
    }

    public List<Video> getVideos()
    {
        return videos;
    }

    public Video getVideo()
    {
        return videos.size() == 1 ? videos.get( 0 ) : null;
    }

    public long getModified()
    {
        if ( videos.size() > 0 )
        {
            long modified = Long.MAX_VALUE;

            for ( Video video : videos )
                modified = Math.min( modified, video.getModified() );

            return modified;
        }

        return Long.MIN_VALUE;
    }
}
