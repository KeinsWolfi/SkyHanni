package at.hannibal2.skyhanni.features.mining.glacitemineshaft

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.data.hypixel.chat.event.PartyChatEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.commands.PartyChatCommands.PartyChatCommand
import at.hannibal2.skyhanni.features.webhook.DiscordEmbed
import at.hannibal2.skyhanni.features.webhook.Webhook
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.format

@SkyHanniModule
object MineshaftType {
    private var found = false

    private val config get() = SkyHanniMod.feature.mining.glaciteMineshaft

    private val profileStorage get() = ProfileStorageData.profileSpecific?.mining?.mineshaft

    private var sinceVang: Int
        get() = profileStorage?.mineshaftsEnteredSinceVanguard ?: 0
        set(value) {
            profileStorage?.mineshaftsEnteredSinceVanguard = value
        }

    private var timeSinceVang: SimpleTimeMark
        get() = profileStorage?.lastVanguardTime ?: SimpleTimeMark.farPast()
        set(value) {
            profileStorage?.lastVanguardTime = value
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
        builder.append("Found a ${type.displayName} mineshaft" + LorenzColor.YELLOW.getChatColor() + "!")

        if (type == MineshaftTypes.FAIR) {
            TitleManager.sendTitle(LorenzColor.WHITE.getChatColor() + "VANGUARD")
            builder.append(
                " It took " +
                    LorenzColor.RED.getChatColor() +
                    timeSinceVang.passedSince().format() +
                    LorenzColor.YELLOW.getChatColor() +
                    " and " +
                    LorenzColor.RED.getChatColor() +
                    "$sinceVang " +
                    LorenzColor.YELLOW.getChatColor() +
                    if (sinceVang == 1) "mineshaft " else "mineshafts " +
                        "entered to get a Vanguard."
            )

            ChatUtils.chat(
                "It took " +
                    LorenzColor.RED.getChatColor() +
                    timeSinceVang.passedSince().format() +
                    LorenzColor.YELLOW.getChatColor() +
                    " and " +
                    LorenzColor.RED.getChatColor() +
                    "$sinceVang " +
                    LorenzColor.YELLOW.getChatColor() +
                    if (sinceVang == 1) "mineshaft " else "mineshafts " +
                        "entered to get a Vanguard."
            )

            if (config.sendWebhookOnVanguardMineshaft) {
                Webhook().addEmbed(
                    DiscordEmbed(
                        title = "Vanguard Mineshaft Found!",
                        description = "It took **${timeSinceVang.passedSince().format()}**" +
                            " and **$sinceVang** mineshafts entered to get a Vanguard.",
                        color = 0xFFFFFF,
                        timestamp = SimpleTimeMark.now().toString(),
                    )
                ).sendTo()
            }

            timeSinceVang = SimpleTimeMark.now()

            sinceVang = 0
        } else {
            if (timeSinceVang.isFarPast()) {
                timeSinceVang = SimpleTimeMark.now()
            }

            sinceVang++
            val mineshaftText = if (sinceVang == 1) "mineshaft" else "mineshafts"
            ChatUtils.chat(
                "${LorenzColor.RED.getChatColor()}$sinceVang" +
                    " ${LorenzColor.YELLOW.getChatColor()}$mineshaftText" +
                    " since ${LorenzColor.WHITE.getChatColor()}Vanguard"
            )
        }

        ChatUtils.chat(builder.toString())
        HypixelCommands.partyChat(builder.toString().removeColor())
    }

    private val allCommands = listOf(
        PartyChatCommand(
            listOf("sincevang", "sincevanguard"),
            { true },
            requiresPartyLead = false,
            executable = {
                HypixelCommands.partyChat(
                    "It has been ${timeSinceVang.passedSince().format()} and " +
                        "$sinceVang mineshafts since the last Vanguard."
                )
            },
        ),
    )

    private val indexedChatCommands = buildMap {
        for (command in allCommands) {
            for (name in command.names) {
                put(name.lowercase(), command)
            }
        }
    }

    @HandleEvent
    fun onPartyChat(event: PartyChatEvent) {
        val message = event.message
        if (message.firstOrNull() != '!') return
        val commandA = message.substring(1).substringBefore(' ')
        val command = indexedChatCommands[commandA.lowercase()] ?: return

        command.executable(event)
    }

    enum class MineshaftTypes(val color: LorenzColor, val rawName: String) {
        TOPA(LorenzColor.YELLOW, "Topaz"),
        SAPP(LorenzColor.BLUE, "Sapphire"),
        AMET(LorenzColor.DARK_PURPLE, "Amethyst"),
        AMBE(LorenzColor.GOLD, "Amber"),
        JADE(LorenzColor.GREEN, "Jade"),
        TITA(LorenzColor.GRAY, "Titanium"),
        UMBE(LorenzColor.GOLD, "Umber"),
        TUNG(LorenzColor.DARK_GRAY, "Tungsten"),
        FAIR(LorenzColor.WHITE, "Vanguard"),
        RUBY(LorenzColor.RED, "Ruby"),
        ONYX(LorenzColor.BLACK, "Onyx"),
        AQUA(LorenzColor.DARK_BLUE, "Aquamarine"),
        CITR(LorenzColor.YELLOW, "Citrine"),
        PERI(LorenzColor.DARK_GREEN, "Peridot"),
        JASP(LorenzColor.LIGHT_PURPLE, "Jasper"),
        OPAL(LorenzColor.WHITE, "Opal"),
        ;

        val displayName: String = color.getChatColor() + rawName
    }
}
