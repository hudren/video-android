package com.hudren.homevideo;

import com.hudren.homevideo.model.Video;

import java.util.List;

/**
 * Actions for interacting with videos.
 */
public interface IVideoActivity
{
    boolean canCast();

    void startStreaming( Video video );

    void startDownloading( List<Video> videos );

    void startCasting( Video video );
}
