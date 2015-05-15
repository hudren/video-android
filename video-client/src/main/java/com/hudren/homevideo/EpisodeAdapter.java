package com.hudren.homevideo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hudren.homevideo.model.Episode;

import java.util.List;

/**
 * Adapter for an episode list. The checked episode will be highlighted in bold.
 */
public class EpisodeAdapter
        extends ArrayAdapter<Episode>
{
    public EpisodeAdapter( Context context, int resource, List<Episode> episodes )
    {
        super( context, resource, episodes );
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        View view = convertView;
        if ( view == null )
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            view = vi.inflate( android.R.layout.simple_list_item_1, null );
        }

        Episode episode = getItem( position );

        TextView textView = (TextView) view.findViewById( android.R.id.text1 );
        textView.setText( episode.getListTitle() );

        int checked = ((ListView) parent).getCheckedItemPosition();
        int style = checked == position ? R.style.selectedEpisode : R.style.normalEpisode;
        textView.setTextAppearance( getContext(), style );

        return view;
    }
}
