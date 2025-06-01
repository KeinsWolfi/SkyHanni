package at.hannibal2.skyhanni.events.render

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.tileentity.TileEntityChest

open class RenderChestEvent(
    var chest: TileEntityChest,
    var x: Double,
    var y: Double,
    var z: Double,
    var partialTicks: Float
) : SkyHanniEvent() {
    class Pre(
        chest: TileEntityChest,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float
    ) : RenderChestEvent(chest, x, y, z, partialTicks)

    class Post(
        chest: TileEntityChest,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float
    ) : RenderChestEvent(chest, x, y, z, partialTicks)
}
