package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.misc.cosmetic.RotatingChestConfig
import at.hannibal2.skyhanni.events.render.RenderChestEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.random.Random

@SkyHanniModule
object RotatingChests {

    /** one rotation state per chest that has appeared on-screen */
    private val rotationStates = mutableMapOf<BlockPos, ChestRotationState>()

    private val config get() = SkyHanniMod.feature.misc.rotatingChest

    // ──────────────────────────────────────────────────────────────────────────────
    //  Event bridge – fired by your mixin
    // ──────────────────────────────────────────────────────────────────────────────
    @HandleEvent
    fun onChestRender(event: RenderChestEvent) {
        if (!config.enabled) return

        if (!event.chest.hasWorldObj()) return

        when (event) {
            is RenderChestEvent.Pre  -> onPre(event)
            is RenderChestEvent.Post -> onPost(event)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    //  Pre-render: advance state, set ESP, push matrix, apply rotation
    // ──────────────────────────────────────────────────────────────────────────────
    private fun onPre(e: RenderChestEvent.Pre) {
        val pos   = e.chest.pos
        val state = rotationStates.getOrPut(pos) { ChestRotationState() }
        state.advance(config.randomRotation, config.rotationSpeed, config.horizontalRotation, config.verticalRotation)

        // ESP
        if (config.chestESP) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL)
            GlStateManager.enablePolygonOffset()
            GlStateManager.doPolygonOffset(1f, -1_000_000f)
            GlStateManager.color(1f, 1f, 1f, 1f)
        }

        // rotation around the centre of the block

        val (cx, cy, cz) = e.chest.renderCentre(e.partialTicks)
        GlStateManager.pushMatrix()
        GlStateManager.translate(cx, cy, cz)
        GlStateManager.rotate(state.yaw,   0f, 1f, 0f)   // Y-axis
        GlStateManager.rotate(state.pitch, 1f, 0f, 0f)   // X-axis
        GlStateManager.translate(-cx, -cy, -cz)
    }

    private fun onPost(@Suppress("UNUSED_PARAMETER") e: RenderChestEvent.Post) {
        GlStateManager.popMatrix()

        if (config.chestESP) {
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL)
            GlStateManager.doPolygonOffset(1f, 1_000_000f)
            GlStateManager.disablePolygonOffset()
        }
    }

    private class ChestRotationState {

        // current angles (deg) – what we finally add to the matrix
        var yaw = 0f          // horizontal (Y-axis)
        var pitch = 0f          // vertical   (X-axis)

        // current angular velocity (deg / s)
        private var yawVel = 0f
        private var pitchVel = 0f

        // last chosen *acceleration* direction on each axis (±1)
        private var yawDir = if (Random.nextBoolean()) 1f else -1f
        private var pitchDir = if (Random.nextBoolean()) 1f else -1f

        private var lastNs = System.nanoTime()

        fun advance(randomRotation: Boolean, rotationSpeed: Float, horizontalRotation: Float, verticalRotation: Float) {
            val nowNs = System.nanoTime()
            val dt = ((nowNs - lastNs) * 1e-9).toFloat() // seconds
            lastNs = nowNs
            if (dt <= 0f) return

            if (randomRotation) {
                // ──────────────────────────────────────────────────────────────
                // 1) pick new acceleration directions with an 80 % “stickiness”
                //    (20 % chance to flip)
                // ──────────────────────────────────────────────────────────────
                if (Random.nextFloat() < 0.05f) yawDir = -yawDir
                if (Random.nextFloat() < 0.05f) pitchDir = -pitchDir

                // ──────────────────────────────────────────────────────────────
                // 2) integrate a *constant* acceleration in those directions
                //    ACC controls how quickly we reach ±MAX_VEL.
                // ──────────────────────────────────────────────────────────────
                val ACC = 45f // deg / s²
                val MAX_VEL = 360f // deg / s

                yawVel = (yawVel + yawDir * ACC * dt).coerceIn(-MAX_VEL, MAX_VEL)
                pitchVel = (pitchVel + pitchDir * ACC * dt).coerceIn(-MAX_VEL, MAX_VEL)

            } else {
                // deterministic mode – velocity is whatever the sliders say
                yawVel   = horizontalRotation
                pitchVel = verticalRotation
            }

            val speedDiv = rotationSpeed.coerceAtLeast(1f)
            yaw   = (yaw   + yawVel   * dt / speedDiv) % 360f
            pitch = (pitch + pitchVel * dt / speedDiv) % 360f
        }
    }

    private fun getCameraPos(partialTicks: Float): Triple<Double, Double, Double> {
        val camera = Minecraft.getMinecraft().renderViewEntity
        val lastX = camera.lastTickPosX
        val lastY = camera.lastTickPosY
        val lastZ = camera.lastTickPosZ

        val x = lastX + (camera.posX - lastX) * partialTicks
        val y = lastY + (camera.posY - lastY) * partialTicks
        val z = lastZ + (camera.posZ - lastZ) * partialTicks

        return Triple(x, y, z)
    }


    /** Returns the geometric centre of this chest (single or double) in
     *  *render-space* (the same coordinate system as event.x/y/z). */
    private fun TileEntityChest.renderCentre(partialTicks: Float): Triple<Float, Float, Float> {
        // World position of chest center
        val neighbor = adjacentChestZNeg ?: adjacentChestZPos
        ?: adjacentChestXNeg ?: adjacentChestXPos

        val cxWorld = if (neighbor != null)
            ((pos.x + neighbor.pos.x) / 2.0) + 0.5
        else
            pos.x + 0.5

        val czWorld = if (neighbor != null)
            ((pos.z + neighbor.pos.z) / 2.0) + 0.5
        else
            pos.z + 0.5

        val cyWorld = pos.y + 0.5

        // Subtract interpolated camera position
        val (camX, camY, camZ) = getCameraPos(partialTicks)
        val rx = (cxWorld - camX).toFloat()
        val ry = (cyWorld - camY).toFloat()
        val rz = (czWorld - camZ).toFloat()

        return Triple(rx, ry, rz)
    }
}
