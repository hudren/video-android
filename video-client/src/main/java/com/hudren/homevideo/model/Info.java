package com.hudren.homevideo.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public List<String> categories = Collections.emptyList();
    public List<String> subjects = Collections.emptyList();
    public List<String> genres = Collections.emptyList();
    public List<String> directors = Collections.emptyList();
    public List<String> stars = Collections.emptyList();
    public List<String> actors = Collections.emptyList();
    public List<String> languages = Collections.emptyList();

    public Map<Integer, SeasonInfo> seasons = Collections.emptyMap();

    public String getSeasonTitle( Integer season )
    {
        SeasonInfo seasonInfo = seasons.get( season );
        if ( seasonInfo != null )
            return seasonInfo.title;

        return null;
    }

    public String getEpisodeTitle( Integer season, Integer episode )
    {
        SeasonInfo seasonInfo = seasons.get( season );
        if ( seasonInfo != null )
        {
            EpisodeInfo episodeInfo = seasonInfo.episodes.get( episode );
            if ( episodeInfo != null )
                return episodeInfo.title;
        }

        return null;
    }

    public static class SeasonInfo implements Serializable
    {
        String title;

        Map<Integer, EpisodeInfo> episodes = Collections.emptyMap();
    }

    public static class EpisodeInfo implements Serializable
    {
        String title;
    }
}
