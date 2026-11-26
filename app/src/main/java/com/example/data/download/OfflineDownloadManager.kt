package com.example.data.download

import android.content.Context
import com.example.data.local.DownloadDao
import com.example.data.local.DownloadEntity
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaItemUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class OfflineDownloadManager(
    private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val downloadsDir: File
        get() = File(context.filesDir, "downloads").apply { if (!exists()) mkdirs() }

    fun startDownload(
        media: MediaItemUi,
        episodeIndex: Int? = null,
        episodeTitle: String? = null,
        streamUrl: String,
        qualityLabel: String,
        languageLabel: String
    ) {
        val downloadId = buildDownloadId(media.id, episodeIndex)
        if (activeJobs[downloadId]?.isActive == true) return

        val safeFileName = "${downloadId.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.mp4"
        val outFile = File(downloadsDir, safeFileName)
        val subtitle = buildString {
            if (episodeTitle != null) {
                append(episodeTitle)
                append(" • ")
            }
            append(qualityLabel)
            append(" • ")
            append(languageLabel)
        }

        val initialEntity = DownloadEntity(
            id = downloadId,
            mediaId = media.id,
            episodeIndex = episodeIndex,
            title = media.title,
            subtitleLabel = subtitle,
            category = media.category.key,
            posterUrl = media.posterUrl,
            localPosterKey = media.localPosterKey,
            sourceUrl = streamUrl,
            localFilePath = outFile.absolutePath,
            qualityLabel = qualityLabel,
            languageLabel = languageLabel,
            status = DownloadStatus.QUEUED.key,
            progress = 0,
            bytesDownloaded = 0L,
            totalBytes = 0L,
            updatedAt = System.currentTimeMillis()
        )

        val job = scope.launch {
            try {
                downloadDao.upsert(initialEntity)

                // If the source is already a local uploaded file, copy it into offline storage cleanly
                if (streamUrl.startsWith("file://") || File(streamUrl).exists()) {
                    val srcFile = File(streamUrl.removePrefix("file://"))
                    if (srcFile.exists()) {
                        val total = srcFile.length().coerceAtLeast(1L)
                        srcFile.inputStream().use { input ->
                            FileOutputStream(outFile).use { output ->
                                val buffer = ByteArray(16 * 1024)
                                var copied = 0L
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1 && isActive) {
                                    output.write(buffer, 0, read)
                                    copied += read
                                    val pct = ((copied * 100L) / total).toInt().coerceIn(0, 100)
                                    downloadDao.updateProgress(
                                        id = downloadId,
                                        status = DownloadStatus.DOWNLOADING.key,
                                        progress = pct,
                                        bytesDownloaded = copied,
                                        totalBytes = total,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                }
                            }
                        }
                        downloadDao.updateProgress(
                            id = downloadId,
                            status = DownloadStatus.COMPLETED.key,
                            progress = 100,
                            bytesDownloaded = outFile.length(),
                            totalBytes = outFile.length(),
                            updatedAt = System.currentTimeMillis()
                        )
                        return@launch
                    }
                }

                // Prefer direct MP4 stream for offline storage if an HLS manifest was passed
                val effectiveUrl = if (streamUrl.contains(".m3u8", ignoreCase = true) && media.streamUrl.isNotBlank() && !media.streamUrl.contains(".m3u8", ignoreCase = true)) {
                    media.streamUrl
                } else {
                    streamUrl
                }

                val request = Request.Builder()
                    .url(effectiveUrl)
                    .header("User-Agent", "CineGlass/1.0 (Android; OfflineDownloader)")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        downloadDao.updateProgress(
                            id = downloadId,
                            status = DownloadStatus.FAILED.key,
                            progress = 0,
                            bytesDownloaded = 0L,
                            totalBytes = 0L,
                            updatedAt = System.currentTimeMillis()
                        )
                        return@launch
                    }

                    val body = response.body ?: run {
                        downloadDao.updateProgress(
                            id = downloadId,
                            status = DownloadStatus.FAILED.key,
                            progress = 0,
                            bytesDownloaded = 0L,
                            totalBytes = 0L,
                            updatedAt = System.currentTimeMillis()
                        )
                        return@launch
                    }

                    val totalBytes = body.contentLength()
                    var downloaded = 0L
                    var lastEmittedPct = -1
                    var lastEmitTime = 0L

                    body.byteStream().use { input ->
                        FileOutputStream(outFile).use { output ->
                            val buffer = ByteArray(16 * 1024)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                if (!isActive) throw CancellationException("Download cancelled")
                                output.write(buffer, 0, bytesRead)
                                downloaded += bytesRead
                                val now = System.currentTimeMillis()
                                val pct = if (totalBytes > 0L) {
                                    ((downloaded * 100L) / totalBytes).toInt().coerceIn(0, 99)
                                } else {
                                    ((downloaded / (256 * 1024L)).toInt() % 95).coerceAtLeast(5)
                                }
                                if (pct != lastEmittedPct && (now - lastEmitTime >= 220L || pct == 99)) {
                                    lastEmittedPct = pct
                                    lastEmitTime = now
                                    downloadDao.updateProgress(
                                        id = downloadId,
                                        status = DownloadStatus.DOWNLOADING.key,
                                        progress = pct,
                                        bytesDownloaded = downloaded,
                                        totalBytes = totalBytes.coerceAtLeast(downloaded),
                                        updatedAt = now
                                    )
                                }
                            }
                            output.flush()
                        }
                    }

                    downloadDao.updateProgress(
                        id = downloadId,
                        status = DownloadStatus.COMPLETED.key,
                        progress = 100,
                        bytesDownloaded = outFile.length(),
                        totalBytes = outFile.length(),
                        updatedAt = System.currentTimeMillis()
                    )
                }
            } catch (_: CancellationException) {
                if (outFile.exists()) outFile.delete()
                downloadDao.deleteById(downloadId)
            } catch (_: Exception) {
                if (outFile.exists()) outFile.delete()
                downloadDao.updateProgress(
                    id = downloadId,
                    status = DownloadStatus.FAILED.key,
                    progress = 0,
                    bytesDownloaded = 0L,
                    totalBytes = 0L,
                    updatedAt = System.currentTimeMillis()
                )
            } finally {
                activeJobs.remove(downloadId)
            }
        }
        activeJobs[downloadId] = job
    }

    fun cancelDownload(downloadId: String) {
        activeJobs.remove(downloadId)?.cancel()
        scope.launch {
            val entity = downloadDao.getById(downloadId)
            if (entity != null) {
                File(entity.localFilePath).takeIf { it.exists() }?.delete()
                downloadDao.deleteById(downloadId)
            }
        }
    }

    suspend fun deleteDownload(downloadId: String) {
        activeJobs.remove(downloadId)?.cancel()
        val entity = downloadDao.getById(downloadId)
        if (entity != null) {
            File(entity.localFilePath).takeIf { it.exists() }?.delete()
        }
        downloadDao.deleteById(downloadId)
    }

    companion object {
        fun buildDownloadId(mediaId: String, episodeIndex: Int?): String =
            "${mediaId}_ep_${episodeIndex ?: 0}"
    }
}
