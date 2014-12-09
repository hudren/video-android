package com.hudren.homevideo.server;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.hudren.homevideo.R;
import com.hudren.homevideo.model.Server;

import java.util.ArrayList;

/**
 * Presents a list of available servers for the user to choose from.
 */
public class ServerDialog extends DialogFragment implements DialogInterface.OnClickListener
{
    public static final String DATA = "servers";

    private VideoServer server;
    private ArrayList< Server > servers;

    public void setServer( VideoServer server )
    {
        this.server = server;
    }

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState )
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder( getActivity() );
        dialog.setTitle( R.string.pick_server );

        Bundle bundle = getArguments();
        servers = (ArrayList< Server >) bundle.getSerializable( DATA );

        // Display the server name
        CharSequence[] choices = new CharSequence[servers.size()];
        for ( int i = 0; i < servers.size(); i++ )
            choices[i] = servers.get( i ).toString();

        dialog.setItems( choices, this );

        return dialog.create();
    }

    @Override
    public void onClick( DialogInterface dialog, int position )
    {
        server.saveServer( servers.get( position ) );

        dialog.dismiss();
    }
}
