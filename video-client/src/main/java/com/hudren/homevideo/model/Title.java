package com.hudren.homevideo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

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
        return info != null && info.title != null ? info.title : title;
    }

    public String getSortingTitle()
    {
        return sorting != null ? sorting : title;
    }

    private static Comparator<Integer> nullSafeComparator = new Comparator<Integer>()
    {
        @Override
        public int compare( Integer lhs, Integer rhs )
        {
            if ( lhs == rhs )
                return 0;
            else if ( lhs == null )
                return -1;
            else if ( rhs == null )
                return 1;

            return lhs.compareTo( rhs );
        }
    };

    public void rankVideos()
    {
        Collections.sort( videos, new Comparator<Video>()
        {
            @Override
            public int compare( Video lhs, Video rhs )
            {
                int comp = nullSafeComparator.compare( lhs.season, rhs.season );

                if ( comp == 0 )
                    comp = nullSafeComparator.compare( lhs.episode, rhs.episode );

                return comp;
            }

        } );
    }

    public List<Video> getVideos()
    {
        return videos;
    }

    public Video getVideo()
    {
        return videos.size() == 1 ? videos.get( 0 ) : null;
    }

    private boolean integerEquals( Integer one, Integer two )
    {
        if ( one == two )
            return true;
        if ( one != null )
            return one.equals( two );

        return false;
    }

    public Video getVideo( Episode episode )
    {
        for ( Video video : videos )
            if ( integerEquals( video.season, episode.season ) && integerEquals( video.episode, episode.index ) )
                return video;

        return null;
    }

    public Video getFirstVideo()
    {
        return videos.size() > 0 ? videos.get( 0 ) : null;
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

    /**
     * Returns whether this title has seasons.
     *
     * @return True, if this title has one or more seasons
     */
    public boolean hasSeasons()
    {
        for ( Video video : videos )
            if ( video.season != null )
                return true;

        return false;
    }

    /**
     * Returns whether there are multiple videos for this title.
     *
     * @return True, if there are multiple parts
     */
    public boolean hasParts()
    {
        return videos.size() > 1 && !hasSeasons();
    }

    /**
     * Returns the season belonging to this title.
     *
     * @return The seasons
     */
    public List<Season> getSeasons()
    {
        ArrayList<Season> seasons = new ArrayList<>();

        // Collect season indexes from videos
        TreeSet<Integer> indexes = new TreeSet<>();
        for ( Video video : videos )
            if ( video.season != null )
                indexes.add( video.season );

        for ( Integer index : indexes )
        {
            Season season = new Season();
            season.index = index;
            season.title = info.getSeasonTitle( index );

            seasons.add( season );
        }

        return seasons;
    }

    /**
     * Returns the episodes belonging to a season.
     *
     * @param season The season
     * @return The season's episodes
     */
    public List<Episode> getEpisodes( Integer season )
    {
        ArrayList<Episode> episodes = new ArrayList<>();

        // Collect episode indexes from videos
        TreeSet<Integer> indexes = new TreeSet<>();
        for ( Video video : videos )
            if ( video.season != null && video.season.equals( season ) && video.episode != null )
                indexes.add( video.episode );

        for ( Integer index : indexes )
        {
            Episode episode = new Episode();
            episode.season = season;
            episode.index = index;
            episode.title = info.getEpisodeTitle( season, index );

            if ( episode.title == null )
                episode.title = getVideo( episode ).episodeTitle;

            episodes.add( episode );
        }

        return episodes;
    }

    /**
     * Returns the parts belonging to a movie.
     *
     * @return The movie parts
     */
    public List<Episode> getParts()
    {
        ArrayList<Episode> episodes = new ArrayList<>();

        // Collect episode indexes from videos
        TreeSet<Integer> indexes = new TreeSet<>();
        for ( Video video : videos )
            if ( video.season == null && video.episode != null )
                indexes.add( video.episode );

        for ( Integer index : indexes )
        {
            Episode episode = new Episode();
            episode.index = index;
            episode.title = info.getEpisodeTitle( null, index );

            episodes.add( episode );
        }

        return episodes;
    }

    /**
     * Returns the full title including season and episode.
     *
     * @param video The video belonging to this title
     * @return The full title
     */
    public String getFullTitle( Video video )
    {
        String text = getTitle();

        if ( video != null && video.episode != null )
            text += " - " + getEpisodeTitle( video );

        return text;
    }

    public String getEpisodeTitle( Video video )
    {
        String text = null;

        if ( video.episode != null )
        {
            String episodeTitle = info.getEpisodeTitle( video.season, video.episode );
            if ( episodeTitle == null )
                episodeTitle = video.episodeTitle;

            if ( video.season != null )
            {
                if ( episodeTitle != null )
                    text = video.season + "." + video.episode + " " + episodeTitle;
                else
                    text = "S" + video.season + " E" + video.episode;
            }
            else
            {
                if ( episodeTitle != null )
                    text = video.episode + ". " + episodeTitle;
                else
                    text = "Part " + video.episode;
            }
        }

        return text;
    }
}
