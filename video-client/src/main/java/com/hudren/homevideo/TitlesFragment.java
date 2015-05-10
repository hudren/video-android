package com.hudren.homevideo;

import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.hudren.homevideo.model.Container;
import com.hudren.homevideo.model.FormatUtils;
import com.hudren.homevideo.model.Title;
import com.hudren.homevideo.model.Video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a ListView to display and interact with videos.
 */
public class TitlesFragment extends ListFragment implements IVideoFragment
{
    private TitlesAdapter adapter;

    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        View rootView = inflater.inflate( R.layout.fragment_video, container, false );

        adapter = new TitlesAdapter( getActivity() );
        setListAdapter( adapter );

        onPreferencesChanged();

        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState )
    {
        super.onActivityCreated( savedInstanceState );

        ListView view = getListView();
        view.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE_MODAL );
        view.setMultiChoiceModeListener( new ActionModeListener() );
    }

    /**
     * Displays the videos.
     *
     * @param titles The titles to be displayed
     */
    public void setTitles( List<Title> titles )
    {
        adapter.setTitles( titles );
    }

    /**
     * Updates the video list based on user preferences.
     */
    public void onPreferencesChanged()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getActivity() );

        adapter.setHighQualityStreaming( prefs.getBoolean( "stream_highest_quality", false ) );

        String order = prefs.getString( "sort_videos", "MOST_RECENT" );
        adapter.setSortOrder( TitlesAdapter.SortOrder.valueOf( order ) );
    }

    /**
     * Sets whether a device is available for casting.
     *
     * @param available True, if casting is possible
     */
    public void setCastAvailable( boolean available )
    {
        adapter.setShowCastIndicators( available );
    }

    /**
     * Sets whether this devices is connected to a server.
     *
     * @param connected True, if connected
     */
    public void setConnected( boolean connected )
    {
        adapter.setShowIndicators( connected );
    }

    /**
     * Streams or casts the video when the user taps.
     *
     * @param list     The list view
     * @param view     The item view
     * @param position The item position
     * @param id       The item id
     */
    @Override
    public void onListItemClick( ListView list, View view, int position, long id )
    {
        Title title = (Title) adapter.getItem( position );
        Video video = title.getVideo();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getActivity() );
        if ( video != null && prefs.getBoolean( "quick_play", false ) )
        {
            IVideoActivity activity = (IVideoActivity) getActivity();
            activity.play( title, video );
        }
        else
        {
            Intent intent = new Intent( "com.hudren.homevideo.VIEW_TITLE" );
            intent.putExtra( "title", title );
            startActivity( intent );
        }
    }

    /**
     * Returns the videos currently selected by the user.
     *
     * @return The list of videos, possibly empty
     */
    public List<Video> getSelectedVideos()
    {
        ListView listView = getListView();

        int len = listView.getCount();
        SparseBooleanArray checked = listView.getCheckedItemPositions();

        List<Video> videos = new ArrayList<>();
        for ( int i = 0; i < len; i++ )
        {
            if ( checked.get( i ) )
            {
                Title title = (Title) adapter.getItem( i );

                // Only provide action for titles with a single video
                if ( title.videos.size() == 1 )
                    videos.add( title.videos.get( 0 ) );
            }
        }

        return videos;
    }

    /**
     * Handles the list multi-selection process with a contextual action bar.
     */
    private class ActionModeListener implements AbsListView.MultiChoiceModeListener
    {
        private long downloadSize = 0;

        @Override
        public void onItemCheckedStateChanged( ActionMode mode, int position, long id, boolean checked )
        {
            Title title = (Title) adapter.getItem( position );
            Video video = title.getVideo();

            if ( video != null )
            {
                Container container = video.getDownload();
                if ( container != null )
                {
                    if ( checked )
                        downloadSize += container.size;
                    else
                        downloadSize -= container.size;

                    if ( downloadSize > 0 )
                        mode.setTitle( FormatUtils.sizeOf( downloadSize ) + " of " + FormatUtils.sizeOf( availableSize() ) + " available" );
                    else
                        mode.setTitle( null );
                }
            }
        }

        @Override
        public boolean onCreateActionMode( ActionMode mode, Menu menu )
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate( R.menu.action, menu );
            return true;
        }

        @Override
        public boolean onPrepareActionMode( ActionMode mode, Menu menu )
        {
            return false;
        }

        @Override
        public boolean onActionItemClicked( ActionMode mode, MenuItem item )
        {
            switch ( item.getItemId() )
            {
            case R.id.action_download:
                IVideoActivity activity = (IVideoActivity) getActivity();
                activity.startDownloading( getSelectedVideos() );

                mode.finish();
                return true;

            default:
                return false;
            }
        }

        @Override
        public void onDestroyActionMode( ActionMode mode )
        {
            downloadSize = 0;
        }
    }

    /**
     * Returns the available number of bytes on the data volume.
     *
     * @return The number of bytes available
     */
    private static long availableSize()
    {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs( path.getPath() );
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }
}
