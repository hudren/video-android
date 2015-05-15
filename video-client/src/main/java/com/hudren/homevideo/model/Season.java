package com.hudren.homevideo.model;

/**
 * Represents a single season.
 */
public class Season
{
    public Integer index;
    public String title;

    public String getListTitle()
    {
        String text = "Season " + index;

        if ( title != null )
            text += " - " + title;

        return text;
    }

    @Override
    public String toString()
    {
        return getListTitle();
    }
}
