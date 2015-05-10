package com.hudren.homevideo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.hudren.homevideo.model.Container;
import com.hudren.homevideo.model.Info;
import com.hudren.homevideo.model.Title;
import com.hudren.homevideo.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a single title.
 */
public class TitleActivity extends VideoActivity
{
    private Title title;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        Intent intent = getIntent();
        title = (Title) intent.getExtras().getSerializable( "title" );

        setTitle( title.getTitle() );
        setContentView( R.layout.fragment_title );
    }

    @Override
    public void onContentChanged()
    {
        super.onContentChanged();

        NetworkImageView image = (NetworkImageView) findViewById( R.id.poster );
        if ( image != null )
            image.setImageUrl( title.poster, VideoApp.getImageLoader() );

        Info info = title.info;

        TextView textView = (TextView) findViewById( R.id.plot );
        if ( textView != null )
            textView.setText( info.plot );

        textView = (TextView) findViewById( R.id.year );
        if ( textView != null )
            textView.setText( String.valueOf( info.year ) );

        textView = (TextView) findViewById( R.id.rated );
        if ( textView != null )
            textView.setText( info.rated );

        textView = (TextView) findViewById( R.id.duration );
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

        Video video = title.getVideo();
        if ( video != null )
            showContainers( R.id.containers, video.getContainers() );
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
        TextView view = (TextView) findViewById( viewId );
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
            }
            else
                hideView( viewId );
        }
    }

    /**
     * Populate the table containing information about the available containers.
     *
     * @param viewId The view
     * @param containers The containers for a single video
     */
    private void showContainers( int viewId, List<Container> containers )
    {
        TableLayout layout = (TableLayout) findViewById( viewId );
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
                    TableRow row = new TableRow( this );
                    row.setLayoutParams( new TableRow.LayoutParams( TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT ) );

                    addCell( row, container.dimension );

                    if ( includeLanguage )
                        addCell( row, container.language );

                    addCell( row, container.video );
                    addCell( row, container.audio );
                    addCell( row, container.getFileSize() );
                    addCell( row, container.getBitrate() );
                    addCell( row, container.filetype.toUpperCase() );

                    layout.addView( row );
                }
            }
            else
                hideView( viewId );
        }
    }

    /**
     * Adds a cell containing the text.
     *
     * @param row The row for the cell
     * @param value The text value
     */
    private void addCell( TableRow row, String value )
    {
        TextView text = new TextView( this );
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
     * Removed the view from the display.
     *
     * @param viewId The view to remove
     */
    private void hideView( int viewId )
    {
        TextView view = (TextView) findViewById( viewId );
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

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.title, menu );

        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        if ( item.getItemId() == R.id.action_play )
            play( title, title.getVideo() );

        return super.onOptionsItemSelected( item );
    }
}
