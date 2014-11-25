package com.hudren.homevideo;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by jeff on 11/17/14.
 */
public class DownloadMonitor extends AsyncTask< Long, Void, Void >
{
    @SuppressWarnings("unused")
    private final static String TAG = "DownloadMonitor";

    private final Context context;

    private final DownloadManager manager;

    public DownloadMonitor( Context context )
    {
        this.context = context;

        this.manager = (DownloadManager) context.getSystemService( Context.DOWNLOAD_SERVICE );
    }

    @Override
    protected Void doInBackground( Long... params )
    {
        long id = params[0];

        boolean done = false;
        while ( !done )
        {
            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById( id );

            Cursor cursor = manager.query( query );
            if ( cursor.moveToFirst() )
            {
                int columnIndex = cursor.getColumnIndex( DownloadManager.COLUMN_STATUS );
                int status = cursor.getInt( columnIndex );
                int columnReason = cursor.getColumnIndex( DownloadManager.COLUMN_REASON );
                int reason = cursor.getInt( columnReason );

                switch ( status )
                {
                case DownloadManager.STATUS_FAILED:
                    String failedReason = "";

                    switch ( reason )
                    {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        failedReason = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        failedReason = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        failedReason = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        failedReason = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        failedReason = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        failedReason = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        failedReason = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        failedReason = "ERROR_UNKNOWN";
                        break;
                    }

                    Log.w( TAG, "download failed: " + failedReason );
                    done = true;
                    break;

                case DownloadManager.STATUS_PAUSED:
                    String pausedReason = "";

                    switch ( reason )
                    {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        pausedReason = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        pausedReason = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        pausedReason = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        pausedReason = "PAUSED_WAITING_TO_RETRY";
                        break;
                    }

                    Log.d( TAG, "download paused: " + pausedReason );
                    break;

                case DownloadManager.STATUS_PENDING:
                    Log.d( TAG, "download pending" );
                    break;

                case DownloadManager.STATUS_RUNNING:
                    // Log.d( TAG, "download running" );
                    break;

                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.i( TAG, "download successful" );
                    done = true;
                    break;
                }
            }
        }

        return null;
    }
}
