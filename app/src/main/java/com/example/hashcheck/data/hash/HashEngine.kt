package com.example.hashcheck.data.hash

import com.example.hashcheck.data.model.HashAlgorithm
import com.example.hashcheck.data.model.HashResult
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.system.measureTimeMillis

/**
 * Core cryptographic hash engine utilizing Java's built-in MessageDigest API.
 * Pure Kotlin/Java implementation designed to be fast, memory-efficient, and unit-testable.
 */
object HashEngine {

    private const val BUFFER_SIZE = 64 * 1024 // 64 KB buffer for streaming

    private val HEX_CHARS_LOWER = "0123456789abcdef".toCharArray()
    private val HEX_CHARS_UPPER = "0123456789ABCDEF".toCharArray()

    /**
     * Converts a byte array to its hexadecimal representation.
     */
    fun bytesToHex(bytes: ByteArray, uppercase: Boolean = false): String {
        val hexChars = if (uppercase) HEX_CHARS_UPPER else HEX_CHARS_LOWER
        val result = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            result[i * 2] = hexChars[v ushr 4]
            result[i * 2 + 1] = hexChars[v and 0x0F]
        }
        return String(result)
    }

    /**
     * Calculates the hash of a plain text string using UTF-8 encoding.
     */
    fun calculateTextHash(
        text: String,
        algorithm: HashAlgorithm,
        uppercase: Boolean = false
    ): HashResult {
        return try {
            val digest = MessageDigest.getInstance(algorithm.algorithmName)
            val bytes = text.toByteArray(StandardCharsets.UTF_8)
            var hashBytes: ByteArray
            val elapsed = measureTimeMillis {
                hashBytes = digest.digest(bytes)
            }
            HashResult(
                algorithm = algorithm,
                hashHex = bytesToHex(hashBytes, uppercase),
                executionTimeMs = elapsed
            )
        } catch (e: NoSuchAlgorithmException) {
            HashResult(
                algorithm = algorithm,
                hashHex = "",
                errorMessage = "Algorithm ${algorithm.displayName} is not supported on this device."
            )
        } catch (e: Exception) {
            HashResult(
                algorithm = algorithm,
                hashHex = "",
                errorMessage = e.localizedMessage ?: "Unknown error occurred"
            )
        }
    }

    /**
     * Calculates hashes for multiple algorithms from a single text input using UTF-8.
     */
    fun calculateTextHashes(
        text: String,
        algorithms: Set<HashAlgorithm>,
        uppercase: Boolean = false
    ): List<HashResult> {
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        return algorithms.sortedBy { it.bitLength }.map { algorithm ->
            try {
                val digest = MessageDigest.getInstance(algorithm.algorithmName)
                var hashBytes: ByteArray
                val elapsed = measureTimeMillis {
                    hashBytes = digest.digest(bytes)
                }
                HashResult(
                    algorithm = algorithm,
                    hashHex = bytesToHex(hashBytes, uppercase),
                    executionTimeMs = elapsed
                )
            } catch (e: NoSuchAlgorithmException) {
                HashResult(
                    algorithm = algorithm,
                    hashHex = "",
                    errorMessage = "Algorithm ${algorithm.displayName} is not supported."
                )
            } catch (e: Exception) {
                HashResult(
                    algorithm = algorithm,
                    hashHex = "",
                    errorMessage = e.localizedMessage ?: "Calculation failed"
                )
            }
        }
    }

    /**
     * Calculates hashes incrementally from an InputStream using a buffered stream.
     * Computes all requested digests in a single streaming pass to maximize performance
     * and minimize memory allocation.
     *
     * @param inputStream Source stream (will be read and closed or caller can close).
     * @param algorithms Algorithms to compute concurrently.
     * @param uppercase Formatting preference.
     * @param isCancelled Check function for cancellation support.
     * @param onProgress Callback invoked periodically with total bytes processed so far.
     */
    fun calculateStreamHashes(
        inputStream: InputStream,
        algorithms: Set<HashAlgorithm>,
        uppercase: Boolean = false,
        isCancelled: () -> Boolean = { false },
        onProgress: ((bytesRead: Long) -> Unit)? = null
    ): List<HashResult> {
        if (algorithms.isEmpty()) return emptyList()

        // Initialize MessageDigest instances
        val digestMap = mutableMapOf<HashAlgorithm, MessageDigest>()
        val failedResults = mutableListOf<HashResult>()

        for (algo in algorithms) {
            try {
                digestMap[algo] = MessageDigest.getInstance(algo.algorithmName)
            } catch (e: NoSuchAlgorithmException) {
                failedResults.add(
                    HashResult(
                        algorithm = algo,
                        hashHex = "",
                        errorMessage = "Algorithm ${algo.displayName} not supported"
                    )
                )
            }
        }

        val buffer = ByteArray(BUFFER_SIZE)
        var totalBytesRead = 0L
        var lastReported = 0L
        val startTime = System.currentTimeMillis()

        try {
            inputStream.use { stream ->
                var read: Int
                while (stream.read(buffer).also { read = it } != -1) {
                    if (isCancelled()) {
                        throw InterruptedException("Hash calculation cancelled by user")
                    }

                    // Update all active digests with this chunk
                    for (digest in digestMap.values) {
                        digest.update(buffer, 0, read)
                    }

                    totalBytesRead += read
                    // Throttle progress updates to every 1MB or so to prevent UI overhead
                    if (totalBytesRead - lastReported >= 512 * 1024) {
                        lastReported = totalBytesRead
                        onProgress?.invoke(totalBytesRead)
                    }
                }
            }
            onProgress?.invoke(totalBytesRead)
        } catch (e: InterruptedException) {
            return algorithms.map { algo ->
                HashResult(
                    algorithm = algo,
                    hashHex = "",
                    errorMessage = "Operation cancelled."
                )
            }
        } catch (e: Exception) {
            return algorithms.map { algo ->
                HashResult(
                    algorithm = algo,
                    hashHex = "",
                    errorMessage = e.localizedMessage ?: "File read error"
                )
            }
        }

        val totalElapsed = System.currentTimeMillis() - startTime
        val successfulResults = digestMap.map { (algo, digest) ->
            val hashBytes = digest.digest()
            HashResult(
                algorithm = algo,
                hashHex = bytesToHex(hashBytes, uppercase),
                executionTimeMs = totalElapsed
            )
        }

        return (successfulResults + failedResults).sortedBy { it.algorithm.bitLength }
    }
}
