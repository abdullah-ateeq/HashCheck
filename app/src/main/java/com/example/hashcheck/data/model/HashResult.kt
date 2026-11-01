package com.example.hashcheck.data.model

/**
 * Result of a hash calculation for a specific algorithm.
 *
 * @param algorithm The cryptographic algorithm used.
 * @param hashHex The hexadecimal string representation of the hash digest.
 * @param executionTimeMs Calculation time in milliseconds.
 * @param errorMessage Any error encountered during calculation (null if successful).
 */
data class HashResult(
    val algorithm: HashAlgorithm,
    val hashHex: String,
    val executionTimeMs: Long = 0L,
    val errorMessage: String? = null
) {
    val isSuccess: Boolean
        get() = errorMessage == null && hashHex.isNotBlank()
}
