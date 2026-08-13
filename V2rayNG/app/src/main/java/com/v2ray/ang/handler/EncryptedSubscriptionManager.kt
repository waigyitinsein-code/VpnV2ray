package com.v2ray.ang.handler

import android.util.Log
import com.v2ray.ang.AppConfig
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.dto.entities.SubscriptionItem
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Handles the versioned AES-GCM subscription envelope used by this fork.
 *
 * Envelope format: v1:<12-byte nonce as hex>:<ciphertext-and-tag as hex>
 * The payload is UTF-8 text containing standard V2Ray/Xray subscription links.
 */
object EncryptedSubscriptionManager {
    private const val ENVELOPE_VERSION = "v1"
    private const val NONCE_BYTES = 12
    private const val GCM_TAG_BITS = 128

    fun ensureDefaultSubscription() {
        val sourceUrl = AppConfig.ENCRYPTED_SUBSCRIPTION_URL
        val exists = MmkvManager.decodeSubscriptions().any { it.subscription.url == sourceUrl }
        if (exists) return

        val item = SubscriptionItem(
            remarks = AppConfig.ENCRYPTED_SUBSCRIPTION_REMARKS,
            url = sourceUrl,
            enabled = true,
            autoUpdate = true,
            updateInterval = AppConfig.ENCRYPTED_SUBSCRIPTION_UPDATE_INTERVAL_MINUTES,
            allowInsecureUrl = false,
            userAgent = "VPNV2Ray/${BuildConfig.VERSION_NAME}"
        )
        MmkvManager.encodeSubscription("", item)
        Log.i(AppConfig.TAG, "Encrypted subscription source registered")
    }

    /**
     * Returns normal text unchanged. Only the v1 envelope is decrypted.
     * No plaintext, key, nonce, or ciphertext is written to logs.
     */
    fun decryptIfEncrypted(content: String): String? {
        val trimmed = content.trim()
        if (!trimmed.startsWith("$ENVELOPE_VERSION:")) return content

        return runCatching {
            val sections = trimmed.split(':')
            require(sections.size == 3 && sections[0] == ENVELOPE_VERSION) {
                "Invalid encrypted subscription envelope"
            }

            val nonce = sections[1].decodeHex()
            val ciphertext = sections[2].decodeHex()
            require(nonce.size == NONCE_BYTES) { "Invalid encrypted subscription nonce" }
            require(ciphertext.size > 16) { "Invalid encrypted subscription payload" }

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val key = SecretKeySpec(AppConfig.ENCRYPTED_SUBSCRIPTION_KEY_HEX.decodeHex(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, nonce))
            String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
        }.onFailure {
            Log.w(AppConfig.TAG, "Unable to decrypt encrypted subscription source")
        }.getOrNull()
    }

    private fun String.decodeHex(): ByteArray {
        require(length % 2 == 0) { "Hex value must contain an even number of characters" }
        return chunked(2).map { value ->
            value.toIntOrNull(16)?.toByte()
                ?: throw IllegalArgumentException("Hex value contains an invalid character")
        }.toByteArray()
    }
}
