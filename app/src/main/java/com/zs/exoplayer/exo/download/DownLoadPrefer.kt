package com.fpc.com.exo.download

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.*
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.zs.exoplayer.exo.download.DownloadTracker
import com.zs.exoplayer.exo.util.FileUtils
import java.io.File

/**
 * @author Created by fpc on 2019-08-12 10:39.
 * @description
 */
class DownLoadPrefer {
    var context: Context? = null
    var downloadManager: DownloadManager? = null
    var downloadTracker: DownloadTracker? = null

    private var databaseProvider: DatabaseProvider? = null
    private var downloadCache: Cache? = null
    fun initManagerAndTracker(context: Context) {
        this.context = context

        databaseProvider = ExoDatabaseProvider(context)
        downloadCache = SimpleCache(
            File(FileUtils.getAppRootPath("maifeng"), DOWNLOAD_CONTENT_DIRECTORY),
            NoOpCacheEvictor(),
            databaseProvider
        )

        val downloadIndex = DefaultDownloadIndex(databaseProvider,"softball")
        upgradeActionFile(
            DOWNLOAD_ACTION_FILE, downloadIndex, /* addNewDownloadsAsCompleted= */ false
        )
        upgradeActionFile(
            DOWNLOAD_TRACKER_ACTION_FILE, downloadIndex, /* addNewDownloadsAsCompleted= */ true
        )
        val downloaderConstructorHelper = DownloaderConstructorHelper(downloadCache, buildHttpDataSourceFactory())
        downloadManager = DownloadManager(
            context, downloadIndex, DefaultDownloaderFactory(downloaderConstructorHelper)
        )
        downloadTracker = DownloadTracker(/* context= */context, buildDataSourceFactory(), downloadManager)
    }

    private fun upgradeActionFile(
        fileName: String,
        downloadIndex: DefaultDownloadIndex,
        addNewDownloadsAsCompleted: Boolean
    ) {
        try {
            ActionFileUpgradeUtil.upgradeAndDelete(
                File(FileUtils.getAppRootPath("maifeng"), fileName),
                /* downloadIdProvider= */ null,
                downloadIndex,
                /* deleteOnFailure= */ true,
                addNewDownloadsAsCompleted
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upgrade action file: $fileName", e)
        }

    }

    /** Returns a [DataSource.Factory].  */
    fun buildDataSourceFactory(): DataSource.Factory {
        val upstreamFactory = DefaultDataSourceFactory(context, buildHttpDataSourceFactory())
        return buildReadOnlyCacheDataSource(upstreamFactory, downloadCache!!)
    }

    protected fun buildReadOnlyCacheDataSource(
        upstreamFactory: DataSource.Factory, cache: Cache
    ): CacheDataSourceFactory {
        return CacheDataSourceFactory(
            cache,
            upstreamFactory,
            FileDataSourceFactory(),
            /* eventListener= */ null,
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
        )/* cacheWriteDataSinkFactory= */
    }

    /** Returns a [HttpDataSource.Factory].  */
    fun buildHttpDataSourceFactory(): HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory(Util.getUserAgent(context, "BigBang_Exo"))
    }

    fun download(ctx: Context, uri: String, listener: DownloadTracker.Listener) {

        downloadTracker?.addListener(listener)
        downloadTracker?.toggleDownload("视频", Uri.parse(uri), null, DefaultRenderersFactory(ctx))

        DownloadHelper.forProgressive(Uri.parse(uri)).periodCount
    }

    private object SingletonHolder {
        val holder = DownLoadPrefer()
    }

    companion object {
        const val TAG = "DownloadHelper"
        private val DOWNLOAD_ACTION_FILE = "actions"
        private val DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions"
        private val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
        @JvmStatic
        val instance = SingletonHolder.holder
    }
}
