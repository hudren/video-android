package com.hudren.homevideo.model;

/**
 * Represents a video within a season.
 */
public class Episode
{
    public Integer season;
    public Integer index;
    public String title;

    public String getListTitle()
    {
        if ( season != null )
            return title != null ? index + ". " + title : "Episode " + index;
        else
            return "Part " + index + (title != null ? " - " + title : "");
    }

    @Override
    public String toString()
    {
        return getListTitle();
    }
}
