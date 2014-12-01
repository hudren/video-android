package com.hudren.homevideo;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SettingsActivity extends PreferenceActivity
{
    private Toolbar toolbar;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        addPreferencesFromResource( R.xml.preferences );
        showListValue( this, "sort_videos" );

        toolbar.setTitle( getTitle() );
    }

    @Override
    public void setContentView( int layoutResID )
    {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from( this ).inflate( R.layout.activity_settings, new LinearLayout( this ), false );

        toolbar = (Toolbar) contentView.findViewById( R.id.toolbar );
        toolbar.setNavigationOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                finish();
            }
        } );

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById( R.id.content_wrapper );
        LayoutInflater.from( this ).inflate( layoutResID, contentWrapper, true );

        getWindow().setContentView( contentView );
    }

    private static void showListValue( PreferenceActivity activity, final String preferenceName )
    {
        final ListPreference list = (ListPreference) activity.findPreference( preferenceName );

        if ( list != null )
        {
            // Set initial value
            list.setSummary( list.getEntry() );

            // Register listener for value changes
            list.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener()
            {

                @Override
                public boolean onPreferenceChange( Preference preference, Object newValue )
                {
                    if ( preference instanceof ListPreference )
                    {
                        ListPreference list = (ListPreference) preference;
                        CharSequence entry = list.getEntries()[list.findIndexOfValue( newValue.toString() )];

                        list.setSummary( entry );
                    }
                    else
                        preference.setSummary( newValue.toString() );

                    return true;
                }

            } );
        }
    }
}
