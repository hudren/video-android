package com.hudren.homevideo.model;

import java.io.Serializable;

/**
 * Represents the saved position for a video when casting.
 */
public class Position implements Serializable
{
    public String title;
    public long position;
    public long modified;

    public Position( String title )
    {
        this.title = title;
    }

    @Override
    public String toString()
    {
        return title + " " + position;
    }
}
