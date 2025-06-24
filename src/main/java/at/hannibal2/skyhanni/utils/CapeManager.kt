package at.hannibal2.skyhanni.utils

//#if MC < 1.21
import net.minecraft.client.entity.AbstractClientPlayer
//#else
//$$ import net.minecraft.client.render.entity.state.PlayerEntityRenderState
//#endif
import net.minecraft.util.ResourceLocation

object CapeManager {
    fun getCapeResource(capeType: CapeType): ResourceLocation {
        //#if MC < 1.21
        return ResourceLocation("skyhanni", capeType.resourcePath)
        //#else
        //$$ return Identifier.of("skyhanni", capeType.resourcePath)
        //#endif
    }

    fun shouldRenderCape(
        //#if MC < 1.21
        player: AbstractClientPlayer
        //#else
        //$$ player: PlayerEntityRenderState
        //#endif
    ) {
    }

    enum class CapeType(val resourcePath: String) {
        SKYHANNI("textures/capes/firm_static.png")
    }
}
