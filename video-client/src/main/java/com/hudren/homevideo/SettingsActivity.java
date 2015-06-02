package com.hudren.homevideo;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
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

    @Override
    public View onCreateView( String name, Context context, AttributeSet attrs )
    {
        final View result = super.onCreateView( name, context, attrs );
        if ( result != null )
            return result;

        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP )
        {
            if ( "EditText".equals( name ) )
                return new AppCompatEditText( this, attrs );
            else if ( "Spinner".equals( name ) )
                return new AppCompatSpinner( this, attrs );
            else if ( "CheckBox".equals( name ) )
                return new AppCompatCheckBox( this, attrs );
            else if ( "RadioButton".equals( name ) )
                return new AppCompatRadioButton( this, attrs );
            else if ( "CheckedTextView".equals( name ) )
                return new AppCompatCheckedTextView( this, attrs );
        }

        return null;
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
