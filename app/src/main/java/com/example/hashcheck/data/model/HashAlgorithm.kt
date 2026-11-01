package com.example.hashcheck.data.model

/**
 * Supported cryptographic hash algorithms.
 *
 * @param algorithmName The standard name recognized by Java's MessageDigest.
 * @param bitLength Output digest length in bits.
 * @param displayName User-friendly name.
 * @param isLegacy Whether this algorithm has known cryptographic vulnerabilities and is deprecated for security-sensitive integrity.
 * @param isRecommended Whether this algorithm is recommended for modern general-purpose hashing.
 * @param securityNote Educational note regarding its cryptographic status.
 */
enum class HashAlgorithm(
    val algorithmName: String,
    val bitLength: Int,
    val displayName: String,
    val isLegacy: Boolean,
    val isRecommended: Boolean,
    val securityNote: String
) {
    MD5(
        algorithmName = "MD5",
        bitLength = 128,
        displayName = "MD5",
        isLegacy = true,
        isRecommended = false,
        securityNote = "Legacy algorithm broken against collision attacks. Unsuitable for security-sensitive integrity checks or digital signatures."
    ),
    SHA1(
        algorithmName = "SHA-1",
        bitLength = 160,
        displayName = "SHA-1",
        isLegacy = true,
        isRecommended = false,
        securityNote = "Legacy algorithm vulnerable to collision attacks (e.g. SHAttered). Deprecated by NIST for secure applications."
    ),
    SHA224(
        algorithmName = "SHA-224",
        bitLength = 224,
        displayName = "SHA-224",
        isLegacy = false,
        isRecommended = true,
        securityNote = "Member of SHA-2 family. A 224-bit truncated variant of SHA-256 offering strong collision resistance."
    ),
    SHA256(
        algorithmName = "SHA-256",
        bitLength = 256,
        displayName = "SHA-256",
        isLegacy = false,
        isRecommended = true,
        securityNote = "Industry standard general-purpose hash algorithm from the SHA-2 family. Highly recommended for file verification and data integrity."
    ),
    SHA384(
        algorithmName = "SHA-384",
        bitLength = 384,
        displayName = "SHA-384",
        isLegacy = false,
        isRecommended = true,
        securityNote = "High-security member of the SHA-2 family. A truncated variant of SHA-512 offering high resistance against attacks."
    ),
    SHA512(
        algorithmName = "SHA-512",
        bitLength = 512,
        displayName = "SHA-512",
        isLegacy = false,
        isRecommended = true,
        securityNote = "Strongest member of the SHA-2 family with 512-bit output. Highly recommended for maximum security integrity checks."
    );

    companion object {
        val ALL: Set<HashAlgorithm> = entries.toSet()
        val DEFAULT_SELECTED: Set<HashAlgorithm> = entries.toSet()

        fun fromAlgorithmName(name: String): HashAlgorithm? {
            return entries.firstOrNull {
                it.algorithmName.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true)
            }
        }
    }
}
