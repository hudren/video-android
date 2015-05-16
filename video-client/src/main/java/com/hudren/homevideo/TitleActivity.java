package com.hudren.homevideo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.hudren.homevideo.model.Title;
import com.hudren.homevideo.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a single title.
 */
public class TitleActivity
        extends VideoActivity
        implements IVideoActivity
{

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        Intent intent = getIntent();
        title = (Title) intent.getExtras().getSerializable( "title" );

        setContentView( R.layout.activity_title );
        initCasting();
    }

    public Title getVideoTitle()
    {
        return title;
    }

    private void selectVideo( Video video )
    {
        this.video = video;

        setTitle( title.getFullTitle( video ) );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.title, menu );

        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public void onVideoSelected( Video video )
    {
        selectVideo( video );
    }
}
