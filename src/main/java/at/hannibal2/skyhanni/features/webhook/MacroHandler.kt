package at.hannibal2.skyhanni.features.webhook

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketSentEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ScreenshotUtil
import net.minecraft.network.play.client.C01PacketChatMessage

@SkyHanniModule
object MacroHandler {
    private val config get() = SkyHanniMod.feature.webhook

    @HandleEvent
    fun onSendMessageToServerPacket(event: PacketSentEvent) {
        val packet = event.packet as? C01PacketChatMessage ?: return

        val message = packet.message
        if (!config.macroWebhooks) return
        if (message.startsWith("macrowebhookbegin")) {
            val screenShot = ScreenshotUtil.captureScreenshot()
            Webhook().addEmbed(
                DiscordEmbed(
                    title = "Macro Started: ${message.removePrefix("macrowebhookbegin").trim()}",
                    image = EmbedImage("attachment://${screenShot.fileName}"),
                )
            ).sendWebhookWithFile(
                file = screenShot.path.toFile(),
            )
        } else if (message.startsWith("macrowebhookend")) {
            val screenShot = ScreenshotUtil.captureScreenshot()
            Webhook().addEmbed(
                DiscordEmbed(
                    title = "Macro Ended: ${message.removePrefix("macrowebhookend").trim()}",
                    image = EmbedImage("attachment://${screenShot.fileName}"),
                )
            ).sendWebhookWithFile(
                file = screenShot.path.toFile()
            )
        }
    }
}
