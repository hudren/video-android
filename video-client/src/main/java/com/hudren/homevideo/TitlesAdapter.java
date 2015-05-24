package com.hudren.homevideo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.hudren.homevideo.model.FormatUtils;
import com.hudren.homevideo.model.Info;
import com.hudren.homevideo.model.Season;
import com.hudren.homevideo.model.Title;
import com.hudren.homevideo.model.Video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Adapter used for displaying the titles in a list.
 */
public class TitlesAdapter extends BaseAdapter
{
    private final LayoutInflater inflater;

    private String userLanguage;

    private boolean streamHighQuality;
    private boolean showIndicators;
    private boolean showCastIndicators;

    enum SortOrder
    {
        ALPHABETICAL, MOST_RECENT, OLDEST
    }

    private SortOrder order = SortOrder.MOST_RECENT;

    private List<Title> titles = new ArrayList<>();

    public TitlesAdapter( Context context )
    {
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        userLanguage = Locale.getDefault().getDisplayLanguage();
    }

    /**
     * Sets whether the streaming indicator should indicate when lower quality streams are not
     * available.
     *
     * @param streamHighQuality True, if high bandwidth titles are stream-able
     */
    public void setHighQualityStreaming( boolean streamHighQuality )
    {
        if ( streamHighQuality != this.streamHighQuality )
        {
            this.streamHighQuality = streamHighQuality;

            notifyDataSetChanged();
        }
    }

    /**
     * Sets whether the indicators should show operations based on being connected to a server.
     *
     * @param show True, if the indicators should be shown
     */
    public void setShowIndicators( boolean show )
    {
        if ( show != showIndicators )
        {
            showIndicators = show;

            notifyDataSetChanged();
        }
    }

    /**
     * Sets whether the cast indicators should be shown for titles that can be cast.
     *
     * @param show True, if the indicators should be shown
     */
    public void setShowCastIndicators( boolean show )
    {
        if ( show != showCastIndicators )
        {
            showCastIndicators = show;

            notifyDataSetChanged();
        }
    }

    /**
     * Sets the titles to be displayed in the list.
     *
     * @param titles The list of views
     */
    public void setTitles( List<Title> titles )
    {
        this.titles = titles;

        sortTitles();

        notifyDataSetChanged();
    }

    public void setSortOrder( SortOrder order )
    {
        if ( order != this.order )
        {
            this.order = order;

            sortTitles();

            notifyDataSetChanged();
        }
    }

    private void sortTitles()
    {
        if ( order == SortOrder.ALPHABETICAL )
        {
            Collections.sort( titles, new Comparator<Title>()
            {
                @Override
                public int compare( Title lhs, Title rhs )
                {
                    return lhs.getSortingTitle().compareTo( rhs.getSortingTitle() );
                }

            } );
        }
        else if ( order == SortOrder.MOST_RECENT )
        {
            Collections.sort( titles, Collections.reverseOrder( new Comparator<Title>()
            {
                @Override
                public int compare( Title lhs, Title rhs )
                {
                    return Long.valueOf( lhs.getModified() ).compareTo( rhs.getModified() );
                }

            } ) );
        }
        else if ( order == SortOrder.OLDEST )
        {
            Collections.sort( titles, new Comparator<Title>()
            {
                @Override
                public int compare( Title lhs, Title rhs )
                {
                    return Long.valueOf( lhs.getModified() ).compareTo( rhs.getModified() );
                }

            } );
        }
    }

    @Override
    public int getCount()
    {
        return titles.size();
    }

    @Override
    public Object getItem( int position )
    {
        return titles.get( position );
    }

    @Override
    public long getItemId( int position )
    {
        return position;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        View view = convertView;
        if ( view == null )
            view = inflater.inflate( R.layout.video_list_item, parent, false );

        bindView( position, view );

        return view;
    }

    /**
     * Populates the view with the item information.
     *
     * @param position The item position
     * @param view     The view associated with the position
     */
    private void bindView( int position, View view )
    {
        Title title = (Title) getItem( position );
        Video video = title.getVideo();
        Info info = title.info;

        // Main text
        TextView text1 = (TextView) view.findViewById( android.R.id.text1 );
        text1.setText( title.getTitle() );

        // Subtext
        TextView text2 = (TextView) view.findViewById( android.R.id.text2 );

        String details = "";
        if ( video != null )
        {
            if ( info.year > 0 )
                details += info.year;

            String language = video.getLanguage();
            if ( language != null && language.length() > 0 && !language.equals( userLanguage ) )
                details += "    " + language;

            if ( info.rated != null && info.rated.length() > 0 )
                details += "    " + info.rated;

            if ( info.runtime != null && info.runtime.length() > 0 )
                details += "    " + info.runtime;
        }
        else
        {
            if ( !title.hasSeasons() && info.year > 0 )
                details += info.year;

            List<Season> seasons = title.getSeasons();
            int count = seasons.size();
            if (count > 0)
            {
                ArrayList<Integer> numbers = new ArrayList<>(  );
                for (Season season : seasons)
                numbers.add( season.index );

                if ( count == 1 )
                    details = "Season " + FormatUtils.ranges( numbers );
                else
                    details = "Seasons " + FormatUtils.ranges( numbers );
            }

            if ( info.rated != null )
                details += "    " + info.rated;
        }

        text2.setText( details.trim() );

        // Change visibility of icons
        if ( showIndicators )
        {
            TextView downloaded = (TextView) view.findViewById( R.id.downloaded );
            if ( downloaded != null )
                downloaded.setVisibility( View.INVISIBLE );

            ImageView icon = (ImageView) view.findViewById( R.id.stream );
            if ( icon != null )
                icon.setVisibility( video != null && video.shouldStream( streamHighQuality ) ? View.VISIBLE : View.INVISIBLE );

            icon = (ImageView) view.findViewById( R.id.download );
            if ( icon != null )
                icon.setVisibility( video != null && video.canDownload() ? View.VISIBLE : View.INVISIBLE );

            icon = (ImageView) view.findViewById( R.id.cast );
            if ( icon != null )
                icon.setVisibility( showCastIndicators && video != null && video.canCast() ? View.VISIBLE : View.INVISIBLE );
        }
        else
        {
            TextView downloaded = (TextView) view.findViewById( R.id.downloaded );
            if ( downloaded != null )
                downloaded.setVisibility( video != null && video.isDownloaded() ? View.VISIBLE : View.INVISIBLE );

            ImageView icon = (ImageView) view.findViewById( R.id.stream );
            if ( icon != null )
                icon.setVisibility( View.INVISIBLE );

            icon = (ImageView) view.findViewById( R.id.download );
            if ( icon != null )
                icon.setVisibility( View.INVISIBLE );

            icon = (ImageView) view.findViewById( R.id.cast );
            if ( icon != null )
                icon.setVisibility( View.INVISIBLE );
        }

        NetworkImageView image = (NetworkImageView) view.findViewById( R.id.poster );
        if ( image != null )
            image.setImageUrl( title.poster, VideoApp.getImageLoader() );
    }
}
