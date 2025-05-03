package at.hannibal2.skyhanni.features.mining.glacitemineshaft

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

@SkyHanniModule
object MineshaftType {
    var found = false

    var area: String? = null

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        found = false
    }

    @HandleEvent(onlyOnIsland = IslandType.MINESHAFT)
    fun onSecondPassed(event: SecondPassedEvent) {
        if (found) return

        val matchingLine = ScoreboardData.sidebarLinesFormatted
            .firstOrNull { line -> MineshaftTypes.entries.any { line.contains(it.name) } }
            ?.removeColor() ?: return

        val areaName = matchingLine.split(" ").last().dropLast(1)

        ChatUtils.debug("In area: $areaName")

        val type = MineshaftTypes.entries.firstOrNull { areaName.contains(it.name) } ?: return
        found = true

        ChatUtils.debug("Found a ${type.name} mineshaft! [$areaName]")
        ChatUtils.chat("Found a ${type.displayName} mineshaft!")
        HypixelCommands.partyChat("[SkyHanni] Found a $type mineshaft!")

        if (type == MineshaftTypes.VANGUARD) {
            TitleManager.sendTitle(LorenzColor.WHITE.getChatColor() + "VANGUARD")
        }
    }

    enum class MineshaftTypes(val displayName: String) {
        TOPAZ("Topaz"),
        SAPPHIRE("Sapphire"),
        AMETHYST("Amethyst"),
        AMBER("Amber"),
        JADE("Jade"),
        TITANIUM("Titanium"),
        UMBER("Umber"),
        TUNGSTEN("Tungsten"),
        VANGUARD("Vanguard"),
        RUBY("Ruby"),
        ONYX("Onyx"),
        AQUAMARINE("Aquamarine"),
        CITRINE("Citrine"),
        PERIDOT("Peridot"),
        JASPER("Jasper"),
        OPAL("Opal"),
    }
}
