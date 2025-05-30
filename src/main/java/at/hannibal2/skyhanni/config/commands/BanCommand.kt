package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests

@SkyHanniModule
object BanCommand {
    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shtestserverdisconnect")
        {
            description = "Simulates a server disconnect"
            category = CommandCategory.DEVELOPER_TEST
            callback { SkyHanniDebugsAndTests.simulateServerDisconnect(it) }
        }
    }
}
