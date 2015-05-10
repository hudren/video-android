package com.hudren.homevideo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains metadata for the video.
 */
public class Info implements Serializable
{
    public String title;

    public String type;
    public long year;
    public String rated;
    public String runtime;

    public String plot;

    public List<String> subjects = Collections.emptyList();
    public List<String> genres = Collections.emptyList();
    public List<String> directors = Collections.emptyList();
    public List<String> stars = Collections.emptyList();
    public List<String> actors = Collections.emptyList();
    public List<String> languages = Collections.emptyList();
}
