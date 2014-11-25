package com.hudren.homevideo.model;

import java.io.Serializable;

/**
 * Represents information about an external file containing subtitles.
 */
public class Subtitle implements Serializable
{
    public String title;
    public String language;
    public String filename;
    public String url;
    public String mimetype;
}
