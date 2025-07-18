package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.drawWaypointFilled
import java.awt.Color

@SkyHanniModule
object StructureWaypoints {
    val waypoints = mutableListOf<StructureWaypoint>()

    @HandleEvent
    fun onWorldChange() {
        waypoints.clear()
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (waypoints.isEmpty()) return

        waypoints
            .forEach {
                if (!it.onlyText) {
                    event.drawWaypointFilled(it.location, it.color, seeThroughBlocks = true)
                }
                event.drawDynamicText(it.location, "§e${it.displayName}", 1.0)
            }
    }
}

data class StructureWaypoint(
    val displayName: String,
    val onlyText: Boolean,
    val location: LorenzVec,
    val color: Color,
) {
    override fun toString(): String {
        return "StructureWaypoint(displayName='$displayName', onlyText=$onlyText, location=$location, color=$color)"
    }
}
