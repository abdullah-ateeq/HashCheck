package com.example.hashcheck.data.model

import android.net.Uri

/**
 * Metadata for a selected file to be hashed.
 */
data class FileInfo(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String? = null
) {
    val formattedSize: String
        get() = formatByteSize(sizeBytes)

    companion object {
        fun formatByteSize(bytes: Long): String {
            if (bytes < 0) return "0 B"
            if (bytes < 1024) return "$bytes B"
            val z = (63 - java.lang.Long.numberOfLeadingZeros(bytes)) / 10
            val value = bytes.toDouble() / (1L shl (z * 10))
            val unit = when (z) {
                1 -> "KB"
                2 -> "MB"
                3 -> "GB"
                4 -> "TB"
                else -> "PB"
            }
            return String.format(java.util.Locale.US, "%.2f %s", value, unit)
        }
    }
}
