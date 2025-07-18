package at.hannibal2.skyhanni.features.webhook

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConfigUtils.jumpToEditor
import at.hannibal2.skyhanni.utils.PlayerUtils
import at.hannibal2.skyhanni.utils.api.ApiUtils
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import kotlin.reflect.KMutableProperty0

private val config get() = SkyHanniMod.feature.webhook

data class Webhook(
    val url: String = config.webhookUrl,
    val content: String? = null,
    val username: String = config.webhookUsername.takeIf { it.isNotEmpty() } ?: "SkyHanni",
    @SerializedName("avatar_url") val avatarUrl: String? = config.webhookAvatarUrl.takeIf { it.isNotEmpty() },
    val tts: Boolean? = null,
    var embeds: List<DiscordEmbed>? = null,
    @SerializedName("allowed_mentions") val allowedMentions: AllowedMentions? = null,
    val components: List<Any>? = null,
    @SerializedName("thread_name") val threadName: String? = null
) {
    fun sendTo(webhookUrlToSend: String = config.webhookUrl) {
        val feature: KMutableProperty0<*>
        if (webhookUrlToSend.isEmpty()) {
            feature = config::webhookUrl
            ChatUtils.clickableChat(
                "§cWebhook URL is empty! Click to set it.",
                onClick = { feature.jumpToEditor() },
                hover = "§eClick to set the webhook URL in the config.",
            )
            return
        }

        if (config.onlyWhenAFK && !PlayerUtils.isAFK) {
            ChatUtils.debug("Not sending webhook because not AFK")
            return
        }

        val jsonPayload = Gson().toJson(this)
        // println("Sending JSON: $jsonPayload")

        SkyHanniMod.launchIOCoroutine {
            ApiUtils.postJson(webhookUrlToSend, jsonPayload, "Discord Webhook")
        }
    }

    fun addEmbed(embed: DiscordEmbed): Webhook {
        embeds = embeds?.plus(embed) ?: listOf(embed)
        return this
    }

    /**
     * Sends this webhook together with a single attachment *file*.
     *
     * Fixes applied compared to the original version:
     *  • sets a proper User-Agent (Cloudflare blocks unknown agents → 403)
     *  • uses the correct part name **files[0]** (Discord rejects “file”)
     *  • injects attachment:// URL into the embed if missing
     *  • builds the body in memory so we can set Content-Length
     *  • closes the multipart exactly with `--boundary--` (no trailing CRLF)
     */
    fun sendWebhookWithFile(
        webhookUrl: String = config.webhookUrl,
        file: File
    ) {
        // ─── guard clauses ───────────────────────────────────────────────────────────
        if (webhookUrl.isBlank()) {
            val feature = config::webhookUrl
            ChatUtils.clickableChat(
                "§cWebhook URL is empty! Click to set it.",
                onClick = { feature.jumpToEditor() },
                hover = "§eClick to set the webhook URL in the config."
            )
            return
        }
        if (config.onlyWhenAFK && !PlayerUtils.isAFK) {
            ChatUtils.debug("Not sending webhook because player isn’t AFK")
            return
        }

        // ─── read the file right now and delete it immediately afterwards ────────────
        val fileBytes = file.readBytes() // copy into RAM
        val fileName = file.name
        file.delete() // temp file no longer needed on disk

        // ─── make sure the embed references the coming attachment ───────────────────
        embeds = (embeds ?: emptyList()).map { e ->
            if (e.image == null || !e.image.url.startsWith("attachment://"))
                e.copy(image = EmbedImage("attachment://$fileName"))
            else e
        }

        val payloadJson = Gson().toJson(this)
        val boundary = "----SkyHanniBoundary${System.currentTimeMillis()}"
        val nl = "\r\n"

        // ─── launch on the IO dispatcher provided by SkyHanni ────────────────────────
        SkyHanniMod.launchIOCoroutine {
            // Build multipart body
            val body = ByteArrayOutputStream()
            val writer = body.writer(Charsets.UTF_8)

            // Part 1 – JSON
            writer.append("--$boundary$nl")
            writer.append("Content-Disposition: form-data; name=\"payload_json\"$nl")
            writer.append("Content-Type: application/json; charset=utf-8$nl$nl")
            writer.append(payloadJson).append(nl)

            // Part 2 – attachment bytes (already in memory)
            writer.append("--$boundary$nl")
            writer.append(
                "Content-Disposition: form-data; name=\"files[0]\"; filename=\"$fileName\"$nl"
            )
            writer.append("Content-Type: ${Files.probeContentType(file.toPath()) ?: "application/octet-stream"}$nl$nl")
            writer.flush()
            body.write(fileBytes)
            writer.append(nl)

            // closing boundary
            writer.append("--$boundary--")
            writer.flush()

            val bodyBytes = body.toByteArray()

            // Send the request
            val conn = (URL(webhookUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty(
                    "User-Agent",
                    "SkyHanni/${SkyHanniMod.VERSION} (+https://github.com/Hannibal002/SkyHanni)"
                )
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                setFixedLengthStreamingMode(bodyBytes.size)
            }

            try {
                conn.outputStream.use { it.write(bodyBytes) }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader()?.readText() ?: "<no body>"

                ChatUtils.debug("Webhook HTTP $code → $text")
            } catch (ex: Exception) {
                ChatUtils.debug("Webhook error: ${ex.message}")
            } finally {
                conn.disconnect()
            }
        }
    }
}
