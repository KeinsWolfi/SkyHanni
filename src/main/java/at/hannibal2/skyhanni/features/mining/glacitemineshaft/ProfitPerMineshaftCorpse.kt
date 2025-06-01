package at.hannibal2.skyhanni.features.mining.glacitemineshaft

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.mining.CorpseLootedEvent
import at.hannibal2.skyhanni.features.webhook.DiscordEmbed
import at.hannibal2.skyhanni.features.webhook.EmbedImage
import at.hannibal2.skyhanni.features.webhook.Webhook
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.repoItemName
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.ScreenshotUtil
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.sortedDesc
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ProfitPerMineshaftCorpse {
    private val config get() = SkyHanniMod.feature.mining.mineshaft

    private val config2 get() = SkyHanniMod.feature.mining.glaciteMineshaft.corpseTracker

    @HandleEvent
    fun onCorpseLooted(event: CorpseLootedEvent) {
        if (!config.profitPerCorpseLoot) return
        val loot = event.loot

        var droppedLocket: Boolean = false

        var totalProfit = 0.0
        val map = mutableMapOf<String, Double>()
        for ((name, amount) in loot) {
            if (name == "§bGlacite Powder") continue
            val internalName = NeuInternalName.fromItemNameOrNull(name) ?: continue
            val pricePer = internalName.getPriceOrNull() ?: continue
            val profit = amount * pricePer
            val text = "§eFound $name §8${amount.addSeparators()}x §7(§6${profit.shortFormat()}§7)"
            map[text] = profit
            totalProfit += profit

            if (internalName == "SHATTERED_PENDANT".toInternalName()) {
                droppedLocket = true
                if (config2.playLocketSound) {
                    config2.dropSound.playSound()
                }
            }
        }

        val corpseType = event.corpseType
        val name = corpseType.displayName

        corpseType.key?.let {
            val keyName = it.repoItemName
            val price = it.getPrice()

            map["§cCost: $keyName §7(§c-${price.shortFormat()}§7)"] = -price
            totalProfit -= price
        }

        val hover = map.sortedDesc().keys.toMutableList()
        val profitPrefix = if (totalProfit < 0) "§c" else "§6"
        val totalMessage = "Profit for $name Corpse§e: $profitPrefix${totalProfit.shortFormat()}"
        hover.add("")
        hover.add("§e$totalMessage")
        ChatUtils.hoverableChat(totalMessage, hover)

        DelayedRun.runDelayed(1.seconds) {
            ChatUtils.sendMessageToServer(totalMessage)
        }

        if (config2.corpseWebhookTypes.get().contains(corpseType) && config2.sendWebhookOnCorpseLoot) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("Total profit: **${totalProfit.shortFormat()}**\n")
            map.sortedDesc().forEach {
                stringBuilder.append("\n> + ${it.key.removeColor().replace("Found ", "")} ") // (+ ${it.value.shortFormat()})")
            }

            SkyHanniMod.launchIOCoroutine {
                val screenShot = ScreenshotUtil.captureScreenshot()
                Webhook(
                    content = if (droppedLocket) "@everyone" else ""
                ).addEmbed(
                    DiscordEmbed(
                        title = "${corpseType.displayName.removeColor()} Corpse Looted!",
                        description = stringBuilder.toString(),
                        timestamp = SimpleTimeMark.now().toString(),
                        color = if (totalProfit < 0) 0xFF4444 else 0xFFAA00,
                        image = EmbedImage("attachment://${screenShot.path}"),
                    )
                ).sendWebhookWithFile(file = screenShot.path.toFile())
            }
        }

        if (config2.funnyLapisThingy) {
            if (corpseType == CorpseType.LAPIS && totalProfit > config2.secretNumber) {
                SoundUtils.dropSoundRichMillionaire.playSound()
            }
        }
    }
}
