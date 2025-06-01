package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.RenderChestEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import kotlin.math.atan2
import kotlin.math.sqrt

@SkyHanniModule
object RotatingChests {
    private val config get() = SkyHanniMod.feature.misc.rotatingChest

    @HandleEvent
    fun onChestRender(event: RenderChestEvent) {
        if (!config.enabled) return

        when (event) {
            is RenderChestEvent.Pre -> {
                val player = MinecraftCompat.localPlayerOrNull ?: return

                // Calculate horizontal angle to face the player
                val dx = player.posX - event.x
                val dz = player.posZ - event.z
                val horizontalAngle = Math.toDegrees(atan2(dz, dx)).toFloat() - 90f

                // Calculate vertical angle to face the player
                val dy = player.posY + player.eyeHeight - (event.y + 0.5)
                val distance = sqrt(dx * dx + dz * dz)
                val verticalAngle = Math.toDegrees(atan2(dy, distance)).toFloat()

                if (config.chestESP) {
                    GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL)
                    GlStateManager.color(1f, 1f, 1f, 1f)
                    GlStateManager.enablePolygonOffset()
                    GlStateManager.doPolygonOffset(1f, -1000000f)
                }

                // Translate to chest center
                GlStateManager.translate(
                    event.x.toFloat() + 0.5f,
                    event.y.toFloat() + 0.5f,
                    event.z.toFloat() + 0.5f
                )

                // Apply rotation to face the player
                GL11.glRotatef(-verticalAngle, 1f, 0f, 0f)
                GL11.glRotatef(horizontalAngle, 0f, 1f, 0f)

                // Translate back
                GlStateManager.translate(
                    -(event.x.toFloat() + 0.5f),
                    -(event.y.toFloat() + 0.5f),
                    -(event.z.toFloat() + 0.5f)
                )
            }
            is RenderChestEvent.Post -> {
                if (config.chestESP) {
                    GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL)
                    GlStateManager.doPolygonOffset(1f, 1000000f)
                    GlStateManager.disablePolygonOffset()
                }
            }
        }
    }
}
