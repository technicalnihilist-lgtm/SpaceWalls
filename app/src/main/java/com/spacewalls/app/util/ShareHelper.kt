package com.spacewalls.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.spacewalls.app.data.model.Wallpaper
import java.io.File

/** Shares the local file when it exists, otherwise a fully-attributed text card. */
object ShareHelper {

    fun share(context: Context, wallpaper: Wallpaper) {
        val file = wallpaper.localPath?.let(::File)?.takeIf { it.exists() && it.length() > 0 }
        val intent = if (file != null) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, wallpaper.shareText())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, wallpaper.shareText())
            }
        }

        context.startActivity(
            Intent.createChooser(intent, "Share wallpaper")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun openUrl(context: Context, url: String) {
        if (url.isBlank()) return
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
