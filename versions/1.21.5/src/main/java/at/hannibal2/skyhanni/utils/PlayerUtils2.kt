package at.hannibal2.skyhanni.utils

import net.minecraft.client.MinecraftClient

object PlayerUtils2 {
    private val mc = MinecraftClient.getInstance()

    fun rightClick() {
        (mc as IMinecraftClient).skyhanniRightClick()
    }
}
