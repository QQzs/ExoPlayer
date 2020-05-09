package com.zs.exoplayer

import android.app.Application
import com.zs.exoplayer.exo.util.ContextUtils

/**
 * @Author: zs
 * @Date: 2020-05-09 16:34
 *
 * @Description:
 */
class MyApplication : Application(){

    override fun onCreate() {
        super.onCreate()

        ContextUtils.init(this)
    }
}