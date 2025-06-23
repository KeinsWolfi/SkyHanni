package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.features.webhook.DiscordEmbed
import at.hannibal2.skyhanni.features.webhook.EmbedImage
import at.hannibal2.skyhanni.features.webhook.Webhook
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.ScreenshotUtil
import at.hannibal2.skyhanni.utils.compat.getStandHelmet
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object BanCommand {
    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shtestserverdisconnect") {
            description = "Simulates a server disconnect"
            category = CommandCategory.DEVELOPER_TEST
            callback { SkyHanniDebugsAndTests.simulateServerDisconnect(it) }
        }
        event.register("shcapture") {
            description = "Sends webhook with screenshot"
            category = CommandCategory.DEVELOPER_TEST
            callback {
                val screenShot = ScreenshotUtil.captureScreenshot()
                Webhook(
                    embeds = listOf(
                        DiscordEmbed(
                            title = "Screenshot",
                            description = "Screenshot captured by SkyHanni",
                            image = EmbedImage("attachment://${screenShot.fileName}"),
                        )
                    )
                ).sendWebhookWithFile(file = screenShot.path.toFile())
            }
        }
    }
}
