package at.hannibal2.skyhanni.features.webhook

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketSentEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
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
        val prefix = message.substringBefore(" ")
        when (prefix) {
            "macrowebhookbegin" -> handleMacroBeginWebhook(event, message)
            "macrowebhookend" -> handleMacroEndWebhook(event, message)
            "macrowebhookbuy" -> handleMacroBuyWebhook(event, message)
            else -> return
        }
    }

    private fun handleMacroBeginWebhook(event: PacketSentEvent, message: String) {
        event.cancel()
        val screenShot = ScreenshotUtil.captureScreenshot()
        Webhook().addEmbed(
            DiscordEmbed(
                title = "Macro Started: ${message.removePrefix("macrowebhookbegin").trim()}",
                image = EmbedImage("attachment://${screenShot.fileName}"),
            )
        ).sendWebhookWithFile(
            file = screenShot.path.toFile(),
        )
    }

    private fun handleMacroEndWebhook(event: PacketSentEvent, message: String) {
        event.cancel()
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

    private fun handleMacroBuyWebhook(event: PacketSentEvent, message: String) {
        event.cancel()

        // "macrowebhookbuy " + this.itemToBuy + " " + this.buyAmount + " " + coinamount

        val parts = message.removePrefix("macrowebhookbuy").trim().split(" ")
        if (parts.size < 3) {
            ChatUtils.userError("Invalid macro buy command: $message")
            return
        }

        val itemToBuy = parts[0]
        val buyAmount = parts[1].toIntOrNull() ?: run {
            ChatUtils.userError("Invalid buy amount in macro buy command: $message")
            return
        }
        val coinAmount = parts[2].toIntOrNull() ?: run {
            ChatUtils.userError("Invalid coin amount in macro buy command: $message")
            return
        }
        val screenShot = ScreenshotUtil.captureScreenshot()
        Webhook().addEmbed(
            DiscordEmbed(
                title = "Macro Buy: $itemToBuy",
                description = "Amount: $buyAmount\nCoins: $coinAmount",
                image = EmbedImage("attachment://${screenShot.fileName}"),
            )
        ).sendWebhookWithFile(
            file = screenShot.path.toFile()
        )
    }
}
