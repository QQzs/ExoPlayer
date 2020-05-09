package com.zs.exoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.fpc.com.exo.IStateListener
import com.google.android.exoplayer2.offline.DownloadService.start
import com.zs.exoplayer.exo.ExoPlayHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var mVideoPlayer: ExoPlayHelper? = null

    var mVideoUrl = "https://freshmate-dev-bigbang-pub.oss-cn-beijing.aliyuncs.com/panels/aded5b4474294a150673d0688a867b0d.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

    }

    private fun initView(){

        initPlayer()

        getVideoPlayer().play(mVideoUrl , object : IStateListener{

            override fun onBuffering() {
                super.onBuffering()
            }

            override fun onIdle() {
                super.onIdle()
            }

            override fun onStart() {
                super.onStart()
            }

            override fun onPause() {
                super.onPause()
            }

            override fun onEnd() {
                super.onEnd()
                iv_pause.visibility = View.VISIBLE
            }
        })

        pv_video?.setOnClickListener {
            if (iv_pause.visibility == View.VISIBLE){
                iv_pause.visibility = View.GONE
                getVideoPlayer().start()
            }else{
                iv_pause.visibility = View.VISIBLE
                getVideoPlayer().pause()
            }
        }
    }

    private fun initPlayer(){
        getVideoPlayer().let { player ->
            player.setPlayerView(pv_video)
            pv_video?.useController = false
            player.setLoop(true)
        }
    }

    private fun getVideoPlayer(): ExoPlayHelper {
        if (mVideoPlayer == null) {
            mVideoPlayer = ExoPlayHelper()
        }
        return mVideoPlayer!!
    }

    override fun onDestroy() {
        super.onDestroy()
        getVideoPlayer()?.stop()
        getVideoPlayer()?.release()
    }
}
