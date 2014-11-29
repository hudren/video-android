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

    private boolean more;
    private String userLanguage;

    private List< Video > videos = new ArrayList< Video >();

    public VideoAdapter( Context context )
    {
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        // Display more details?
        WindowManager windowManager = (WindowManager) context.getSystemService( Context.WINDOW_SERVICE );
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics( metrics );

        more = metrics.widthPixels / metrics.density > 420;

        userLanguage = Locale.getDefault().getDisplayLanguage();
    }

    /**
     * Sets the videos to be displayed in the list.
     *
     * @param videos The list of views
     */
    public void setVideos( List< Video > videos )
    {
        // TODO: add sorting to UI
        Collections.sort( videos, Collections.reverseOrder( new Comparator< Video >()
        {
            @Override
            public int compare( Video lhs, Video rhs )
            {
                return Long.valueOf( lhs.getLastModified() ).compareTo( rhs.getLastModified() );
            }

        } ) );

        this.videos = videos;

        notifyDataSetChanged();
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
        String title = video.title;
        if ( video.episode > 0 )
            title += " - Episode " + video.episode;

        text1.setText( title );

        // Subtext
        TextView text2 = (TextView) view.findViewById( android.R.id.text2 );

        String details = video.getDuration();
        details += "    " + video.getQuality();
        String language = video.getLanguage();

        if ( language != null && language.length() > 0 && !language.equals( userLanguage ) )
            details += "    " + language;

        if ( more )
        {
            details += "    " + video.getVideoCodecs();

            String download = video.getDownloadSize();
            if ( download != null && download.length() > 0 )
                details += "    " + video.getDownloadSize();
        }

        text2.setText( details );

        // Change visibility of icons
        ImageView icon = (ImageView) view.findViewById( R.id.download );
        if ( icon != null )
            icon.setVisibility( video.canDownload() ? View.VISIBLE : View.INVISIBLE );

        icon = (ImageView) view.findViewById( R.id.cast );
        if ( icon != null )
            icon.setVisibility( video.canCast() ? View.VISIBLE : View.INVISIBLE );
    }
}
