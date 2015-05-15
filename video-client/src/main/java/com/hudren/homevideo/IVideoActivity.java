package com.hudren.homevideo;

import com.hudren.homevideo.model.Title;
import com.hudren.homevideo.model.Video;

/**
 * Created by jeff on 5/11/15.
 */
public interface IVideoActivity
{
    Title getVideoTitle();

    void onVideoSelected( Video video );
}
