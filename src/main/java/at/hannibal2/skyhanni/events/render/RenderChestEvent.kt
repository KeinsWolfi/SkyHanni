package at.hannibal2.skyhanni.events.render

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
//#if MC < 1.21
import net.minecraft.tileentity.TileEntityChest
//#else
//$$ import net.minecraft.block.entity.ChestBlockEntity
//#endif


open class RenderChestEvent(
    //#if MC < 1.21
    var chest: TileEntityChest,
    //#else
    //$$ chest: ChestBlockEntity,
    //#endif
    var x: Double,
    var y: Double,
    var z: Double,
    var partialTicks: Float
) : SkyHanniEvent() {
    class Pre(
        //#if MC < 1.21
        chest: TileEntityChest,
        //#else
        //$$ chest: ChestBlockEntity,
        //#endif
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float
    ) : RenderChestEvent(chest, x, y, z, partialTicks)

    class Post(
        //#if MC < 1.21
        chest: TileEntityChest,
        //#else
        //$$ chest: ChestBlockEntity,
        //#endif
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float
    ) : RenderChestEvent(chest, x, y, z, partialTicks)
}
