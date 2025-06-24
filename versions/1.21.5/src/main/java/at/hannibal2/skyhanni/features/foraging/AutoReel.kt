package at.hannibal2.skyhanni.features.foraging

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.cleanName
import at.hannibal2.skyhanni.utils.MobUtils.mob
import at.hannibal2.skyhanni.utils.PlayerUtils2
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.PhantomEntity
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AutoReel {

    val config get() = SkyHanniMod.feature.foraging

    var lastClick = SimpleTimeMark.farPast()

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return

        val phantomEntities = EntityUtils.getAllEntities()
            .filterIsInstance<PhantomEntity>()
            .toCollection(mutableListOf())

        if (phantomEntities.isEmpty()) return

        for (phantom in phantomEntities) {
            val mob = phantom.mob ?: continue
            val reel = mob.getNearestReel()
            if (reel == null || !reel.isReel()) continue

            if (lastClick.passedSince() > 4.seconds) {
                PlayerUtils2.rightClick()
                lastClick = SimpleTimeMark.now()
                ChatUtils.debug("Right click executed on reel: ${reel.cleanName()} at ${reel.pos} for ${mob.name}")
            }
        }
    }

    fun isEnabled(): Boolean {
        return config.autoReel
    }

    private fun Mob.getNearestReel(): ArmorStandEntity? {
        val reelEntity = EntityUtils.getAllEntities()
            .filterIsInstance<ArmorStandEntity>()
            .filter { it.isReel() }
            .minByOrNull { this.baseEntity.distanceTo(it) }
        if (reelEntity != null) {
            ChatUtils.debug(
                "Nearest reel: ${reelEntity.cleanName()}" +
                    " at ${reelEntity.pos ?: "unknown"} for ${this.name}" +
                    " with distance ${reelEntity.distanceTo(this.baseEntity)}"
            )
        }
        return reelEntity
    }

    private fun ArmorStandEntity.isReel(): Boolean {
        return this.cleanName() == "REEL"
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register(name = "rightClickTest") {
            description = "Test the right click functionality of AutoReel"
            callback {
                if (lastClick.passedSince() > 1.seconds) {
                    PlayerUtils2.rightClick()
                    lastClick = SimpleTimeMark.now()
                    ChatUtils.debug("Right click executed successfully.")
                }
            }
        }
    }
}
