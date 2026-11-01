package com.example.hashcheck.data.hash

import com.example.hashcheck.data.model.HashAlgorithm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

class HashEngineTest {

    private val quickFox = "The quick brown fox jumps over the lazy dog"

    @Test
    fun testEmptyInputHandling() {
        val md5Result = HashEngine.calculateTextHash("", HashAlgorithm.MD5)
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", md5Result.hashHex)
        assertTrue(md5Result.isSuccess)
        assertNull(md5Result.errorMessage)

        val sha1Result = HashEngine.calculateTextHash("", HashAlgorithm.SHA1)
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", sha1Result.hashHex)

        val sha224Result = HashEngine.calculateTextHash("", HashAlgorithm.SHA224)
        assertEquals("d14a028c2a3a2bc9476102bb288234c415a2b01f828ea62ac5b3e42f", sha224Result.hashHex)

        val sha256Result = HashEngine.calculateTextHash("", HashAlgorithm.SHA256)
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", sha256Result.hashHex)

        val sha384Result = HashEngine.calculateTextHash("", HashAlgorithm.SHA384)
        assertEquals(
            "38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b",
            sha384Result.hashHex
        )

        val sha512Result = HashEngine.calculateTextHash("", HashAlgorithm.SHA512)
        assertEquals(
            "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e",
            sha512Result.hashHex
        )
    }

    @Test
    fun testKnownMD5TestVectors() {
        val result1 = HashEngine.calculateTextHash("abc", HashAlgorithm.MD5)
        assertEquals("900150983cd24fb0d6963f7d28e17f72", result1.hashHex)

        val result2 = HashEngine.calculateTextHash(quickFox, HashAlgorithm.MD5)
        assertEquals("9e107d9d372bb6826bd81d3542a419d6", result2.hashHex)
    }

    @Test
    fun testKnownSHA1TestVectors() {
        val result1 = HashEngine.calculateTextHash("abc", HashAlgorithm.SHA1)
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", result1.hashHex)

        val result2 = HashEngine.calculateTextHash(quickFox, HashAlgorithm.SHA1)
        assertEquals("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", result2.hashHex)
    }

    @Test
    fun testKnownSHA256TestVectors() {
        val result1 = HashEngine.calculateTextHash("abc", HashAlgorithm.SHA256)
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", result1.hashHex)

        val result2 = HashEngine.calculateTextHash(quickFox, HashAlgorithm.SHA256)
        assertEquals("d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592", result2.hashHex)
    }

    @Test
    fun testSHA224Correctness() {
        val result = HashEngine.calculateTextHash(quickFox, HashAlgorithm.SHA224)
        assertEquals("730e109bd7a8a32b1cb9d9a09aa2325d2430587ddbc0c38bad911525", result.hashHex)
    }

    @Test
    fun testSHA384AndSHA512Correctness() {
        val result384 = HashEngine.calculateTextHash(quickFox, HashAlgorithm.SHA384)
        assertEquals(
            "ca737f1014a48f4c0b6dd43cb177b0afd9e5169367544c494011e3317dbf9a509cb1e5dc1e85a941bbee3d7f2afbc9b1",
            result384.hashHex
        )

        val result512 = HashEngine.calculateTextHash(quickFox, HashAlgorithm.SHA512)
        assertEquals(
            "07e547d9586f6a73f73fbac0435ed76951218fb7d0c8d788a309d785436bbb642e93a252a954f23912547d1e8a3b5ed6e1bfd7097821233fa0538f3db854fee6",
            result512.hashHex
        )
    }

    @Test
    fun testUTF8TextEncoding() {
        // Multi-byte Unicode: Greek, Chinese, Emoji
        val unicodeText = "Hello, 世界! 🌍 こんにちは 🚀"
        val textBytes = unicodeText.toByteArray(StandardCharsets.UTF_8)
        assertTrue(textBytes.size > unicodeText.length)

        val hashResult = HashEngine.calculateTextHash(unicodeText, HashAlgorithm.SHA256)
        val streamResult = HashEngine.calculateStreamHashes(
            ByteArrayInputStream(textBytes),
            setOf(HashAlgorithm.SHA256)
        ).first()

        assertEquals(hashResult.hashHex, streamResult.hashHex)
    }

    @Test
    fun testHexadecimalOutputFormatting() {
        // Test lowercase vs uppercase
        val lower = HashEngine.calculateTextHash("test", HashAlgorithm.MD5, uppercase = false)
        val upper = HashEngine.calculateTextHash("test", HashAlgorithm.MD5, uppercase = true)

        assertEquals("098f6bcd4621d373cade4e832627b4f6", lower.hashHex)
        assertEquals("098F6BCD4621D373CADE4E832627B4F6", upper.hashHex)
        assertEquals(lower.hashHex.uppercase(), upper.hashHex)

        // Verify zero-padding (e.g. byte value 0x05 should be "05", not "5")
        val singleByte = byteArrayOf(0x05, 0x0A, 0x00, 0xFF.toByte())
        assertEquals("050a00ff", HashEngine.bytesToHex(singleByte, uppercase = false))
        assertEquals("050A00FF", HashEngine.bytesToHex(singleByte, uppercase = true))
    }

    @Test
    fun testMultipleAlgorithmsFromIdenticalInput() {
        val algorithms = HashAlgorithm.ALL
        val results = HashEngine.calculateTextHashes(quickFox, algorithms, uppercase = false)

        assertEquals(algorithms.size, results.size)
        assertTrue(results.all { it.isSuccess })

        val md5 = results.first { it.algorithm == HashAlgorithm.MD5 }
        val sha256 = results.first { it.algorithm == HashAlgorithm.SHA256 }

        assertEquals("9e107d9d372bb6826bd81d3542a419d6", md5.hashHex)
        assertEquals("d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592", sha256.hashHex)
    }

    @Test
    fun testFileHashingConsistency() {
        val sampleData = "Sample data for stream vs text consistency verification.".toByteArray(StandardCharsets.UTF_8)

        val textResults = HashEngine.calculateTextHashes(String(sampleData, StandardCharsets.UTF_8), HashAlgorithm.ALL)
        val streamResults = HashEngine.calculateStreamHashes(ByteArrayInputStream(sampleData), HashAlgorithm.ALL)

        assertEquals(textResults.size, streamResults.size)
        for (i in textResults.indices) {
            assertEquals(textResults[i].algorithm, streamResults[i].algorithm)
            assertEquals(textResults[i].hashHex, streamResults[i].hashHex)
        }
    }

    @Test
    fun testLargeFileBufferedProcessing() {
        // Generate a 1 MB synthetic stream without allocating 1 MB memory at once
        val pattern = "0123456789ABCDEF".toByteArray(StandardCharsets.US_ASCII)
        val totalBytes = 1024 * 1024 // 1 MB

        class RepeatingPatternInputStream(private val limit: Long) : InputStream() {
            private var current: Long = 0
            override fun read(): Int {
                if (current >= limit) return -1
                val b = pattern[(current % pattern.size).toInt()].toInt() and 0xFF
                current++
                return b
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                if (current >= limit) return -1
                val toRead = minOf(len.toLong(), limit - current).toInt()
                for (i in 0 until toRead) {
                    b[off + i] = pattern[((current + i) % pattern.size).toInt()]
                }
                current += toRead
                return toRead
            }
        }

        var progressReported = 0L
        val results = HashEngine.calculateStreamHashes(
            inputStream = RepeatingPatternInputStream(totalBytes.toLong()),
            algorithms = setOf(HashAlgorithm.SHA256, HashAlgorithm.MD5),
            onProgress = { bytesRead ->
                progressReported = bytesRead
            }
        )

        assertEquals(2, results.size)
        assertTrue(results.all { it.isSuccess })
        assertTrue(progressReported > 0)
    }

    @Test
    fun testCancellationSupport() {
        val infiniteStream = object : InputStream() {
            override fun read(): Int = 0x41
        }

        var cancelled = false
        val results = HashEngine.calculateStreamHashes(
            inputStream = infiniteStream,
            algorithms = setOf(HashAlgorithm.SHA256),
            isCancelled = {
                // Cancel immediately after first check
                val was = cancelled
                cancelled = true
                was
            }
        )

        assertEquals(1, results.size)
        assertFalse(results.first().isSuccess)
        assertEquals("Operation cancelled.", results.first().errorMessage)
    }
}
