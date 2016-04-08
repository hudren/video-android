package com.hudren.homevideo;

import com.hudren.homevideo.model.Title;
import com.hudren.homevideo.model.Video;

/**
 * Interface for activities displaying multiple videos.
 */
public interface IVideoActivity
{
    Title getVideoTitle();

    void onVideoSelected( Video video );
}
