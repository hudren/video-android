package com.hudren.homevideo;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.hudren.homevideo.model.Video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Adapter used for displaying the videos in a list.
 */
public class VideoAdapter extends BaseAdapter
{
    private final LayoutInflater inflater;
    private final ImageLoader imageLoader;

    private boolean more;
    private String userLanguage;

    private boolean streamHighQuality;
    private boolean showIndicators;
    private boolean showCastIndicators;

    enum SortOrder
    {
        ALPHABETICAL, MOST_RECENT, OLDEST;
    }

    private SortOrder order = SortOrder.MOST_RECENT;

    private List<Video> videos = new ArrayList<Video>();

    public VideoAdapter( Context context )
    {
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        imageLoader = NetworkManager.getInstance( context ).getImageLoader();

        // Display more details?
        WindowManager windowManager = (WindowManager) context.getSystemService( Context.WINDOW_SERVICE );
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics( metrics );

        more = metrics.widthPixels / metrics.density > 420;

        userLanguage = Locale.getDefault().getDisplayLanguage();
    }

    /**
     * Sets whether the streaming indicator should indicate when lower quality streams are not
     * available.
     *
     * @param streamHighQuality True, if high bandwidth videos are stream-able
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
     * Sets whether the cast indicators should be shown for videos that can be cast.
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
     * Sets the videos to be displayed in the list.
     *
     * @param videos The list of views
     */
    public void setVideos( List<Video> videos )
    {
        this.videos = videos;

        sortVideos();

        notifyDataSetChanged();
    }

    public void setSortOrder( SortOrder order )
    {
        if ( order != this.order )
        {
            this.order = order;

            sortVideos();

            notifyDataSetChanged();
        }
    }

    private int compareInt( int n1, int n2 )
    {
        if ( n1 == n2 )
            return 0;
        if ( n1 < n2 )
            return -1;
        return 1;
    }

    private void sortVideos()
    {
        if ( order == SortOrder.ALPHABETICAL )
        {
            Collections.sort( videos, new Comparator<Video>()
            {
                @Override
                public int compare( Video lhs, Video rhs )
                {
                    int c = lhs.getSortingTitle().compareTo( rhs.getSortingTitle() );
                    if ( c == 0 )
                        c = compareInt( lhs.season, rhs.season );
                    if ( c == 0 )
                        c = compareInt( lhs.episode, rhs.episode );
                    return c;
                }

            } );
        }
        else if ( order == SortOrder.MOST_RECENT )
        {
            Collections.sort( videos, Collections.reverseOrder( new Comparator<Video>()
            {
                @Override
                public int compare( Video lhs, Video rhs )
                {
                    return Long.valueOf( lhs.getModified() ).compareTo( rhs.getModified() );
                }

            } ) );
        }
        else if ( order == SortOrder.OLDEST )
        {
            Collections.sort( videos, new Comparator<Video>()
            {
                @Override
                public int compare( Video lhs, Video rhs )
                {
                    return Long.valueOf( lhs.getModified() ).compareTo( rhs.getModified() );
                }

            } );
        }
    }

    @Override
    public int getCount()
    {
        return videos.size();
    }

    @Override
    public Object getItem( int position )
    {
        return videos.get( position );
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
        Video video = (Video) getItem( position );

        // Main text
        TextView text1 = (TextView) view.findViewById( android.R.id.text1 );
        text1.setText( video.getFullTitle( more ) );

        // Subtext
        TextView text2 = (TextView) view.findViewById( android.R.id.text2 );

        String details = video.getDuration();
        details += "    " + video.getQuality();
        String language = video.getLanguage();

        if ( language != null && language.length() > 0 && !language.equals( userLanguage ) )
            details += "    " + language;

        if ( more )
        {
            String videoCodecs = video.getVideoCodecs();
            if ( !"H.264".equals( videoCodecs ) )
                details += "    " + videoCodecs;

            String download = video.getDownloadSize();
            if ( download != null && download.length() > 0 )
                details += "    " + video.getDownloadSize();
        }

        text2.setText( details );

        // Change visibility of icons
        if ( showIndicators )
        {
            TextView downloaded = (TextView) view.findViewById( R.id.downloaded );
            if ( downloaded != null )
                downloaded.setVisibility( View.INVISIBLE );

            ImageView icon = (ImageView) view.findViewById( R.id.stream );
            if ( icon != null )
                icon.setVisibility( video.shouldStream( streamHighQuality ) ? View.VISIBLE : View.INVISIBLE );

            icon = (ImageView) view.findViewById( R.id.download );
            if ( icon != null )
                icon.setVisibility( video.canDownload() ? View.VISIBLE : View.INVISIBLE );

            icon = (ImageView) view.findViewById( R.id.cast );
            if ( icon != null )
                icon.setVisibility( showCastIndicators && video.canCast() ? View.VISIBLE : View.INVISIBLE );
        }
        else
        {
            TextView downloaded = (TextView) view.findViewById( R.id.downloaded );
            if ( downloaded != null )
                downloaded.setVisibility( video.isDownloaded() ? View.VISIBLE : View.INVISIBLE );

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
            image.setImageUrl( video.poster, imageLoader );
    }
}
