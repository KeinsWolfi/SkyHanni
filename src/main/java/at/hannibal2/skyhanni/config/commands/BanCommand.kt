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
        event.register("shgetskulltexture") {
            description = "Gets the skull texture of the entity youre looking at."
            category = CommandCategory.DEVELOPER_TEST
            callback {
                val entity = Minecraft.getMinecraft().objectMouseOver.entityHit
                if (entity == null || entity !is EntityArmorStand) {
                    ChatUtils.chat("§cNo entity under your crosshair or the entity is not an armor stand.")
                    return@callback
                }
                val texture = entity.getStandHelmet()?.getSkullTexture()
                if (texture == null) {
                    ChatUtils.chat("§cNo skull texture found for the armor stand.")
                } else {
                    ChatUtils.chat("§aSkull texture: §e$texture")
                }
            }
        }
    }
}
