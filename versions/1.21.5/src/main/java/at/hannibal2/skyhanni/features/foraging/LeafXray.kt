package at.hannibal2.skyhanni.features.foraging

import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.SkillExpGainEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.skillprogress.SkillType
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import net.minecraft.block.LeavesBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.WorldRenderer
import net.minecraft.util.math.ChunkPos
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object LeafXray {

    val config get() = SkyHanniMod.feature.foraging

    private var lastForagingExpGain: SimpleTimeMark = SimpleTimeMark.farPast()

    private var wasForaging = false
    private var dirty = false

    private val worldRenderer: WorldRenderer = MinecraftClient.getInstance().worldRenderer

    @JvmStatic
    fun getAlpha(state: BlockState, pos: BlockPos?): Int {
        if (!isEnabled()) return 255
        if (!isForaging()) return 255
        if (state.block !is LeavesBlock) return 255

        return 0
    }

    private fun isEnabled(): Boolean {
        return config.leafXray
    }

    private fun isForaging(): Boolean {
        return lastForagingExpGain.passedSince() < config.leafXrayCooldown.toDouble().seconds
    }

    @HandleEvent
    fun onSkillExpGain(event: SkillExpGainEvent) {
        if (event.skill != SkillType.FORAGING) return
        lastForagingExpGain = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) {
            if (wasForaging) {
                wasForaging = false
                refreshChunks()
            }
            return
        }

        if (isForaging()) {
            if (!wasForaging) {
                wasForaging = true
                dirty = true
            }
        } else {
            if (wasForaging) {
                wasForaging = false
                dirty = true
            }
        }

        if (dirty) {
            dirty = false
            refreshChunks()
        }
    }

    private fun refreshChunks() {
        worldRenderer.reload()
    }
}
