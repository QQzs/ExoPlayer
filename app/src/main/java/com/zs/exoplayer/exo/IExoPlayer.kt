package com.funduemobile.bigbang.third.exo

import com.fpc.com.exo.IStateListener
import com.google.android.exoplayer2.ui.PlayerView

/**
 *  Created by fpc  2019-08-09  15:29
 */
interface IExoPlayer {

    fun setPlayerView(view: PlayerView?)

    fun play(url: String, listener: IStateListener?,is_hls:Boolean=false)

    fun pause()

    fun start()

    fun stop()

    fun release()

    fun isActive(): Boolean
}