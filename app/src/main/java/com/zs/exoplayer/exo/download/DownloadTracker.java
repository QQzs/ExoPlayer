/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zs.exoplayer.exo.download;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.offline.*;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Tracks media that has been downloaded.
 */
public class DownloadTracker {

    /**
     * Listens for changes in the tracked downloads.
     */
    public interface Listener {

        /**
         * Called when the tracked downloads changed.
         */
        void onDownloadsChanged();
    }

    private static final String TAG = "DownloadTracker";

    private final Context context;
    private final DataSource.Factory dataSourceFactory;
    private final CopyOnWriteArraySet<Listener> listeners;
    private final HashMap<Uri, Download> downloads;
    private final DownloadIndex downloadIndex;

    @Nullable
    private StartDownloadDialogHelper startDownloadDialogHelper;

    public DownloadTracker(
            Context context, DataSource.Factory dataSourceFactory, DownloadManager downloadManager) {
        this.context = context.getApplicationContext();
        this.dataSourceFactory = dataSourceFactory;
        listeners = new CopyOnWriteArraySet<>();
        downloads = new HashMap<>();
        downloadIndex = downloadManager.getDownloadIndex();
        downloadManager.addListener(new DownloadManagerListener());
        loadDownloads();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isDownloaded(Uri uri) {
        Download download = downloads.get(uri);
        return download != null && download.state != Download.STATE_FAILED;
    }

    @SuppressWarnings("unchecked")
    public DownloadRequest getDownloadRequest(Uri uri) {
        Download download = downloads.get(uri);
        return download != null && download.state != Download.STATE_FAILED ? download.request : null;
    }

    public void toggleDownload(
            String name,
            Uri uri,
            String extension,
            RenderersFactory renderersFactory) {
        Download download = downloads.get(uri);
        if (download != null) {
            DownloadService.sendRemoveDownload(
                    context, SimpleDownloadService.class, download.request.id, /* foreground= */ false);
        } else {
            if (startDownloadDialogHelper != null) {
                startDownloadDialogHelper.release();
            }
            startDownloadDialogHelper =
                    new StartDownloadDialogHelper( getDownloadHelper(uri, extension, renderersFactory), name);
        }
    }

    private void loadDownloads() {
        try (DownloadCursor loadedDownloads = downloadIndex.getDownloads()) {
            while (loadedDownloads.moveToNext()) {
                Download download = loadedDownloads.getDownload();
                downloads.put(download.request.uri, download);
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to query downloads", e);
        }
    }

    private DownloadHelper getDownloadHelper(
            Uri uri, String extension, RenderersFactory renderersFactory) {
        int type = Util.inferContentType(uri, extension);
        switch (type) {
            case C.TYPE_DASH:
                return DownloadHelper.forDash(uri, dataSourceFactory, renderersFactory);
            case C.TYPE_SS:
                return DownloadHelper.forSmoothStreaming(uri, dataSourceFactory, renderersFactory);
            case C.TYPE_HLS:
                return DownloadHelper.forHls(uri, dataSourceFactory, renderersFactory);
            case C.TYPE_OTHER:
                return DownloadHelper.forProgressive(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private class DownloadManagerListener implements DownloadManager.Listener {

        @Override
        public void onDownloadChanged(DownloadManager downloadManager, Download download) {
            downloads.put(download.request.uri, download);
            Log.d("=====",download.contentLength+"====="+download.getPercentDownloaded());
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }

        @Override
        public void onDownloadRemoved(DownloadManager downloadManager, Download download) {
            downloads.remove(download.request.uri);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }
    }

    private final class StartDownloadDialogHelper
            implements DownloadHelper.Callback {

        private final DownloadHelper downloadHelper;
        private final String name;


        public StartDownloadDialogHelper(DownloadHelper downloadHelper, String name) {
            this.downloadHelper = downloadHelper;
            this.name = name;
            downloadHelper.prepare(this);
        }

        public void release() {
            downloadHelper.release();
        }

        // DownloadHelper.Callback implementation.

        @Override
        public void onPrepared(DownloadHelper helper) {
            if (helper.getPeriodCount() == 0) {
                Log.d(TAG, "No periods found. Downloading entire stream.");
                startDownload();
                downloadHelper.release();
                return;
            }
//            mappedTrackInfo = downloadHelper.getMappedTrackInfo(/* periodIndex= */ 0);
            Log.d(TAG, "No dialog content. Downloading entire stream.");
            startDownload();
            downloadHelper.release();

        }

        @Override
        public void onPrepareError(DownloadHelper helper, IOException e) {
            Toast.makeText(
                    context.getApplicationContext(),"准备期间发生错误", Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Failed to start download", e);
        }

        // Internal methods.

        private void startDownload() {
            startDownload(buildDownloadRequest());
        }

        private void startDownload(DownloadRequest downloadRequest) {
            DownloadService.sendAddDownload(
                    context, SimpleDownloadService.class, downloadRequest, /* foreground= */ false);
        }

        private DownloadRequest buildDownloadRequest() {
            return downloadHelper.getDownloadRequest(Util.getUtf8Bytes(name));
        }
    }
}
