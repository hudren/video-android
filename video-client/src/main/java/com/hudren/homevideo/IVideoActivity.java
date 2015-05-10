package com.hudren.homevideo;

import com.hudren.homevideo.model.Title;
import com.hudren.homevideo.model.Video;

import java.util.List;

/**
 * Actions for interacting with videos.
 */
public interface IVideoActivity
{
    boolean canCast();

    void play( Title title, Video video );

    void startStreaming( Title title, Video video );

    void startDownloading( List<Video> videos );

    void startCasting( Title title, Video video );
}
