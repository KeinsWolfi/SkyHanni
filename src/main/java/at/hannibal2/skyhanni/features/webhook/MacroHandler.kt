package at.hannibal2.skyhanni.features.webhook

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils

@SkyHanniModule
object MacroHandler {
    private val config get() = SkyHanniMod.feature.webhook

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        val msg = event.message

        if (msg.startsWith("ASDIUAIUSUDZ")) {
            ChatUtils.chat("§c§lMacroHandler: §r§cThis is a test message, please ignore it.")
        }
    }
}
