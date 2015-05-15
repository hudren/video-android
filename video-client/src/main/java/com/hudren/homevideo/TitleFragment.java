package com.hudren.homevideo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.hudren.homevideo.model.Container;
import com.hudren.homevideo.model.Episode;
import com.hudren.homevideo.model.Info;
import com.hudren.homevideo.model.Season;
import com.hudren.homevideo.model.Title;
import com.hudren.homevideo.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the title information (including seasons and episodes).
 */
public class TitleFragment extends Fragment
{
    private Title title;
    private Video video;

    private List<Season> seasons;
    private List<Episode> episodes;

    private int currentSeason;
    private int currentEpisode;

    private EpisodeAdapter adapter;

    private boolean more;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        // Display more details?
        WindowManager windowManager = (WindowManager) getActivity().getSystemService( Context.WINDOW_SERVICE );
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics( metrics );

        more = metrics.widthPixels / metrics.density > 420;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_title, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState )
    {
        super.onActivityCreated( savedInstanceState );

        if ( title == null )
        {
            Activity activity = getActivity();
            if ( activity instanceof IVideoActivity )
                showTitle( ((IVideoActivity) activity).getVideoTitle() );
        }
        else
            showTitle( title );
    }

    public void showTitle( Title title )
    {
        this.title = title;

        seasons = null;
        episodes = null;
        currentSeason = 0;
        currentEpisode = 0;

        if ( getView() != null )
        {
            if ( title != null )
            {
                if ( title.hasSeasons() )
                {
                    seasons = title.getSeasons();

                    // Get first season's episodes
                    if ( seasons.size() > 0 )
                        episodes = title.getEpisodes( seasons.get( 0 ).index );
                }
                else if ( title.hasParts() )
                    episodes = title.getParts();

                selectVideo();
                updateContent();

                showView( R.id.layout );
            }
            else
                hideView( R.id.layout );
        }
    }

    public void updateContent()
    {
        final Info info = title.info;

        final View rootView = getView();
        if ( rootView != null )
        {
            final TextView plot = (TextView) rootView.findViewById( R.id.plot );
            if ( plot != null )
                plot.setText( info.plot );

            final NetworkImageView poster = (NetworkImageView) rootView.findViewById( R.id.poster );
            if ( poster != null )
            {
                if ( poster != null )
                {
                    poster.setImageUrl( title.poster, VideoApp.getImageLoader() );

                    if ( info.plot != null )
                    {
                        poster.addOnLayoutChangeListener( new View.OnLayoutChangeListener()
                        {
                            @Override
                            public void onLayoutChange( View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom )
                            {
                                makeSpan( poster, plot, info.plot );
                            }
                        } );
                    }
                }
                else
                    hideView( R.id.poster );
            }

            TextView textView = (TextView) rootView.findViewById( R.id.year );
            if ( textView != null )
                textView.setText( String.valueOf( info.year ) );

            textView = (TextView) rootView.findViewById( R.id.rated );
            if ( textView != null )
                textView.setText( info.rated );

            textView = (TextView) rootView.findViewById( R.id.duration );
            if ( textView != null )
                textView.setText( info.runtime );

            showList( R.id.subjects, R.string.subjects, info.subjects );
            showList( R.id.genres, R.string.genres, info.genres );
            showList( R.id.directors, R.string.directors, info.directors );
            showList( R.id.cast, R.string.cast, combineLists( info.stars, info.actors ) );

            if ( info.languages != null && !(info.languages.size() == 1 && "English".equals( info.languages.get( 0 ) )) )
                showList( R.id.languages, R.string.languages, info.languages );
            else
                hideView( R.id.languages );

            // Containers
            showContainers( R.id.containers, video.getContainers() );

            // Seasons
            if ( seasons != null && seasons.size() > 0 )
            {
                if ( seasons.size() == 1 )
                {
                    textView = (TextView) rootView.findViewById( R.id.season );
                    textView.setText( seasons.get( 0 ).getListTitle() );

                    showView( R.id.season );
                    hideView( R.id.seasons );
                }
                else
                {
                    Spinner spinner = (Spinner) rootView.findViewById( R.id.seasons );

                    ArrayAdapter<Season> adapter = new ArrayAdapter<>( getActivity(), android.R.layout.simple_spinner_item, seasons );
                    adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
                    spinner.setAdapter( adapter );

                    spinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
                    {
                        @Override
                        public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
                        {
                            if ( position != currentSeason )
                            {
                                currentSeason = position;
                                currentEpisode = 0;

                                Season season = seasons.get( currentSeason );
                                episodes = title.getEpisodes( season.index );

                                if ( TitleFragment.this.adapter != null )
                                {
                                    TitleFragment.this.adapter.setNotifyOnChange( false );
                                    TitleFragment.this.adapter.clear();
                                    TitleFragment.this.adapter.addAll( episodes );
                                    TitleFragment.this.adapter.notifyDataSetChanged();

                                    ListView list = (ListView) rootView.findViewById( R.id.episodes );
                                    setListViewHeightBasedOnChildren( list );

                                    list.setItemChecked( currentEpisode, true );
                                }

                                Episode episode = episodes.get( currentEpisode );
                                Video video = title.getVideo( episode );

                                selectVideo( video );
                            }
                        }

                        @Override
                        public void onNothingSelected( AdapterView<?> parent )
                        {
                            // Do nothing
                        }
                    } );

                    hideView( R.id.season );
                    showView( R.id.seasons );
                }
            }
            else
            {
                hideView( R.id.season );
                hideView( R.id.seasons );
            }

            // Episodes / parts
            if ( episodes != null && episodes.size() > 0 )
            {
                final ListView list = (ListView) rootView.findViewById( R.id.episodes );

                adapter = new EpisodeAdapter( getActivity(), android.R.layout.simple_list_item_1, episodes );
                list.setAdapter( adapter );

                list.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
                list.setItemChecked( 0, true );

                setListViewHeightBasedOnChildren( list );

                list.setOnItemClickListener( new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
                    {
                        if ( position != currentEpisode )
                        {
                            currentEpisode = position;

                            Episode episode = episodes.get( currentEpisode );
                            Video video = title.getVideo( episode );

                            selectVideo( video );

                            list.setItemChecked( position, true );
                            adapter.notifyDataSetChanged();
                        }
                    }
                } );

                showView( R.id.episodes );
            }
            else
                hideView( R.id.episodes );
        }
    }

    private void selectVideo( Video video )
    {
        this.video = video;

        if ( video != null )
            showContainers( R.id.containers, video.getContainers() );

        if ( getActivity() instanceof IVideoActivity )
            ((IVideoActivity) getActivity()).onVideoSelected( video );
    }

    private void selectVideo()
    {
        if ( episodes != null && episodes.size() > 0 )
            selectVideo( title.getVideo( episodes.get( currentEpisode ) ) );
        else
            selectVideo( title.getFirstVideo() );
    }

    private void makeSpan( NetworkImageView poster, TextView plot, String text )
    {
        int height = poster.getMeasuredHeight();
        int width = poster.getMeasuredWidth() + scale( 16 );
        int lines = (int) Math.ceil( height / plot.getPaint().getFontSpacing() );

        TitleLeadingMarginSpan span = new TitleLeadingMarginSpan( lines, width );
        SpannableString string = new SpannableString( text );
        string.setSpan( span, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );

        plot.setText( string );
    }

    public static void setListViewHeightBasedOnChildren( ListView listView )
    {
        ListAdapter listAdapter = listView.getAdapter();
        if ( listAdapter == null )
        {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for ( int i = 0; i < listAdapter.getCount(); i++ )
        {
            View listItem = listAdapter.getView( i, null, listView );
            listItem.measure( 0, 0 );
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams( params );
    }

    /**
     * Loads the label and list of strings into the text view.
     *
     * @param viewId  The text view
     * @param labelId The label
     * @param values  The list of string values
     */
    private void showList( int viewId, int labelId, List<String> values )
    {
        TextView view = (TextView) getView().findViewById( viewId );
        if ( view != null )
        {
            if ( values != null && values.size() > 0 )
            {
                StringBuilder builder = new StringBuilder();
                builder.append( "<b>" );
                builder.append( getResources().getString( labelId ) );
                builder.append( ":</b> " );

                boolean first = true;
                for ( String value : values )
                {
                    if ( !first )
                        builder.append( ", " );
                    else
                        first = false;

                    builder.append( value );
                }

                view.setText( Html.fromHtml( builder.toString() ) );
                showView( viewId );
            }
            else
                hideView( viewId );
        }
    }

    /**
     * Populate the table containing information about the available containers.
     *
     * @param viewId     The view
     * @param containers The containers for a single video
     */
    private void showContainers( int viewId, List<Container> containers )
    {
        View view = getView();
        if ( view != null )
        {
            TableLayout layout = (TableLayout) getView().findViewById( viewId );
            if ( layout != null )
            {
                if ( containers != null && containers.size() > 0 )
                {
                    layout.removeAllViewsInLayout();

                    boolean includeLanguage = false;
                    for ( Container container : containers )
                    {
                        if ( container.language != null && !"English".equals( container.language ) )
                            includeLanguage = true;
                    }

                    for ( Container container : containers )
                    {
                        TableRow row = new TableRow( getActivity() );
                        row.setLayoutParams( new TableRow.LayoutParams( TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT ) );

                        addCell( row, container.dimension );

                        if ( includeLanguage )
                            addCell( row, container.language );

                        addCell( row, container.video );
                        addCell( row, container.audio );

                        if ( more )
                        {
                            addCell( row, container.getFileSize() );
                            addCell( row, container.getBitrate() );
                        }

                        addCell( row, container.filetype.toUpperCase() );

                        layout.addView( row );
                    }
                }
                else
                    hideView( viewId );
            }
        }
    }

    /**
     * Adds a cell containing the text.
     *
     * @param row   The row for the cell
     * @param value The text value
     */
    private void addCell( TableRow row, String value )
    {
        TextView text = new TextView( getActivity() );
        text.setText( value );
        text.setTextColor( Color.GRAY );
        text.setTextSize( TypedValue.COMPLEX_UNIT_SP, 18 );
        row.addView( text );

        TableRow.LayoutParams lp = new TableRow.LayoutParams( TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT );
        lp.setMargins( 0, 0, scale( 16 ), 0 );
        text.setLayoutParams( lp );
    }

    /**
     * Scales the dimension to match the display density.
     *
     * @param dimension The dimension
     * @return The pixels matching the dimension
     */
    private int scale( int dimension )
    {
        return (int) (dimension * getResources().getDisplayMetrics().density);
    }

    /**
     * Return the view to the display.
     *
     * @param viewId The view to show
     */
    private void showView( int viewId )
    {
        View view = getView().findViewById( viewId );
        if ( view != null )
            view.setVisibility( View.VISIBLE );
    }

    /**
     * Remove the view from the display.
     *
     * @param viewId The view to remove
     */
    private void hideView( int viewId )
    {
        View view = getView().findViewById( viewId );
        if ( view != null )
            view.setVisibility( View.GONE );
    }

    /**
     * Combines two lists, avoiding duplicates while maintaining order.
     *
     * @param first  The first list
     * @param second The second list
     * @return The combined list
     */
    public List<String> combineLists( List<String> first, List<String> second )
    {
        List<String> combined = first != null ? new ArrayList<>( first ) : new ArrayList<String>();

        if ( second != null )
            for ( String value : second )
                if ( !combined.contains( value ) )
                    combined.add( value );

        return combined;
    }
}
