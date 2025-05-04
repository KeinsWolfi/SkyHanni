package at.hannibal2.skyhanni.features.mining.glacitemineshaft

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
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
    private var found = false

    private var storage: Int
        get() = ProfileStorageData.profileSpecific?.mining?.mineshaft?.mineshaftsEnteredSinceVanguard ?: 0
        set(value) {
            ProfileStorageData.profileSpecific?.mining?.mineshaft?.mineshaftsEnteredSinceVanguard = value
        }

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

        val builder = StringBuilder()
        builder.append("Found a ${type.displayName} mineshaft!")

        if (type == MineshaftTypes.FAIR) {
            TitleManager.sendTitle(LorenzColor.WHITE.getChatColor() + "VANGUARD")
            builder.append(
                " It took " +
                    LorenzColor.RED.getChatColor() +
                    "$storage " +
                    LorenzColor.YELLOW.getChatColor() +
                    if (storage == 1) "mineshaft " else "mineshafts " +
                        "entered to get a Vanguard."
            )

            ChatUtils.chat(
                "It took " +
                    LorenzColor.RED.getChatColor() +
                    "$storage " +
                    LorenzColor.YELLOW.getChatColor() +
                    if (storage == 1) "mineshaft " else "mineshafts " +
                        "entered to get a Vanguard."
            )

            storage = 0
        } else {
            storage++
            ChatUtils.chat(
                LorenzColor.RED.getChatColor() +
                    "$storage " +
                    LorenzColor.YELLOW.getChatColor() +
                    if (storage == 1) "mineshaft " else "mineshafts " +
                        "since " +
                        LorenzColor.WHITE.getChatColor() +
                        "Vanguard"
            )
        }

        ChatUtils.chat(builder.toString())
        HypixelCommands.partyChat(builder.toString().removeColor())
    }

    enum class MineshaftTypes(val displayName: String) {
        TOPA("Topaz"),
        SAPP("Sapphire"),
        AMET("Amethyst"),
        AMBE("Amber"),
        JADE("Jade"),
        TITA("Titanium"),
        UMBE("Umber"),
        TUNG("Tungsten"),
        FAIR("Vanguard"),
        RUBY("Ruby"),
        ONYX("Onyx"),
        AQUA("Aquamarine"),
        CITR("Citrine"),
        PERI("Peridot"),
        JASP("Jasper"),
        OPAL("Opal"),
    }
}
