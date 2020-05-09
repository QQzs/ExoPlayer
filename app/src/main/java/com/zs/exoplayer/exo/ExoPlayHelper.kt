package com.zs.exoplayer.exo

import android.content.Context
import android.net.Uri
import com.fpc.com.exo.IStateListener
import com.funduemobile.bigbang.third.exo.IExoPlayer
import com.funduemobile.bigbang.third.exo.VideoCache
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.zs.exoplayer.exo.util.ContextUtils
import com.zs.exoplayer.pool.JobManager
import java.util.concurrent.atomic.AtomicBoolean


/**
 *  Created by fpc  2019-08-09  15:19
 */
class ExoPlayHelper : IExoPlayer, Player.EventListener {
    private var cacheDataSourceFactory: CacheDataSourceFactory? = null
    private var dataSourceFactory: DefaultDataSourceFactory? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var simpleCache: SimpleCache? = null

    private var exoPlayer: SimpleExoPlayer? = null

    private var listener: IStateListener? = null

    private var state: Int = STATE_IDLE

    override
    fun setPlayerView(view: PlayerView?) {
        if (exoPlayer == null)
            initPlayer()
        view?.player = exoPlayer
        exoPlayer?.playWhenReady = true
    }

    fun setControllView(view: PlayerControlView?) {
        if (exoPlayer == null)
            initPlayer()
        view?.player = exoPlayer
        exoPlayer?.playWhenReady = true
    }

    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume
    }

    fun getVolume(): Float {
        return exoPlayer?.volume ?: 0f
    }

    override fun play(url: String, listener: IStateListener?,isHls:Boolean) {
        if (exoPlayer == null)
            initPlayer()
        if (listener != null)
            this.listener = listener

        if(isHls){
            val videoSource=HlsMediaSource.Factory(DefaultDataSourceFactory(ContextUtils.sApplicationContext,Util.getUserAgent(ContextUtils.sApplicationContext,"exo_hls"))).createMediaSource(Uri.parse(url))
            exoPlayer?.prepare(videoSource)
        }else{
            val videoSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory, DefaultExtractorsFactory())
                    .createMediaSource(Uri.parse(url))
            exoPlayer?.prepare(videoSource)
        }

    }


    fun addListener(listener: IStateListener?) {
        this.listener = listener
    }

    override fun pause() {
        exoPlayer?.playWhenReady = false
    }

    override fun start() {
        exoPlayer?.playWhenReady = true
    }

    override fun stop() {
        exoPlayer?.playWhenReady = false
        exoPlayer?.stop(true)
    }

    fun seekTo(time: Int) {
        exoPlayer?.seekTo(time * 1000L)
    }
    fun seekTo(time: Float) {
        exoPlayer?.seekTo((time * 1000L).toLong())
    }
    fun seekTo(time: Long) {
        exoPlayer?.seekTo(time)
    }

    fun getTrack(): Long? {
        return exoPlayer?.currentPosition
    }

    override fun release() {
        exoPlayer?.removeListener(this)
        exoPlayer?.release()
        exoPlayer = null
        listener=null
    }

    fun setLoop(loop: Boolean) {
        exoPlayer?.repeatMode = if (loop) SimpleExoPlayer.REPEAT_MODE_ONE else SimpleExoPlayer.REPEAT_MODE_OFF
    }

    override fun isActive(): Boolean {
        return exoPlayer != null && exoPlayer?.playbackState != Player.STATE_IDLE
    }

    constructor() {
        initFactory(ContextUtils.sApplicationContext)
        initPlayer()
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(ContextUtils.sApplicationContext, DefaultRenderersFactory(ContextUtils.sApplicationContext), trackSelector, DefaultLoadControl())
        exoPlayer?.addListener(this)
    }

    private fun initFactory(context: Context) {
        dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "exoPlayHelper"))
        simpleCache = VideoCache.instance.getCache(context)

        cacheDataSourceFactory = CacheDataSourceFactory(simpleCache, dataSourceFactory)
        trackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory())
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                if (playWhenReady) {
                    listener?.onStart()
                    state = STATE_START
                } else {
                    listener?.onPause()
                    state = STATE_PAUSE
                }
                Log.d("===", "STATE_READY")
            }
            Player.STATE_BUFFERING -> {
//                if (playWhenReady) {
                listener?.onBuffering()
                state = STATE_BUFFING
//                } else {
//                    listener?.onPause()
//                }
                Log.d("===", "STATE_BUFFERING")
            }
            Player.STATE_ENDED -> {
                Log.d("===", "STATE_ENDED")
                listener?.onEnd()
                state = STATE_END
            }
            Player.STATE_IDLE -> {
                Log.d("===", "STATE_IDLE")
                listener?.onIdle()
                state = STATE_IDLE
            }
        }

    }

    fun preload(videoUri: String) {
        JobManager.getInstance().submitRunnable {
            val dataSpec = DataSpec(Uri.parse(videoUri), 0, (2 * 1024 * 1024).toLong(), null)
            try {
                CacheUtil.cache(dataSpec, simpleCache, CacheUtil.DEFAULT_CACHE_KEY_FACTORY, cacheDataSourceFactory?.createDataSource(),
                        null, AtomicBoolean(false))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    open fun getState(): Int {
        return state
    }

    companion object {
        const val TAG = "ExoPlayHelper"
        const val STATE_START = 0
        const val STATE_PAUSE = 1
        const val STATE_BUFFING = 2
        const val STATE_IDLE = 3
        const val STATE_END = 4

    }

}