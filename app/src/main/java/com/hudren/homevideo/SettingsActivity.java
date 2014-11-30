package com.hudren.homevideo;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        getFragmentManager().beginTransaction()
                .replace( android.R.id.content, new SettingsFragment() )
                .commit();
    }

    private static void showListValue( PreferenceFragment fragment, final String preferenceName )
    {
        final ListPreference list = (ListPreference) fragment.findPreference( preferenceName );

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

    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            addPreferencesFromResource( R.xml.preferences );

            showListValue( this, "sort_videos" );
        }
    }
}
