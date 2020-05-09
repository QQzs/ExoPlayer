package com.funduemobile.bigbang.third.exo

import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.zs.exoplayer.exo.util.FileUtils
import java.io.File


/**
 *  Created by fpc  2019-10-30  14:36
 */
/**
 * @author ：leo on 2018/12/17 17:58
 *
 *
 * 方法用途 ：视频缓存单例模式
 */
class VideoCache {
    private var sDownloadCache: SimpleCache? = null

    /**
     * @param context
     * @return
     */
    fun getCache(context: Context): SimpleCache {
        if (sDownloadCache == null) {
            sDownloadCache = SimpleCache(File(FileUtils.getAppRootPath("cache")), LeastRecentlyUsedCacheEvictor(512 * 1024 *1024), ExoDatabaseProvider(context))
        }
        return sDownloadCache as SimpleCache
    }


    private object SingletonHolder {
        val holder = VideoCache()
    }
    companion object {
        const val TAG = "VideoCache"
        @JvmStatic
        val instance = SingletonHolder.holder
    }
}
