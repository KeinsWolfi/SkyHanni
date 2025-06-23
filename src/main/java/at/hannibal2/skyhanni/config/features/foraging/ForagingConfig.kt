package at.hannibal2.skyhanni.config.features.foraging

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.OnlyLegacy
import at.hannibal2.skyhanni.config.OnlyModern
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

/**
 * Attention developers:
 * If your feature can only be used on the foraging islands please mark it with @[OnlyModern]
 */
class ForagingConfig {

    @ConfigOption(
        name = "§cNotice",
        desc = "To see all foraging features please launch the game on a modern version of Minecraft with SkyHanni installed.",
    )
    @OnlyLegacy
    @ConfigEditorInfoText
    var notice: String = ""

    @Expose
    @ConfigOption(name = "Foraging Tutorial Quest", desc = "")
    @Accordion
    @OnlyModern
    val tutorialQuest: ForagingTutorialQuestConfig = ForagingTutorialQuestConfig()

    @Expose
    @ConfigOption(name = "Moonglade Beacon", desc = "Settings for the moonglade beacon.")
    @OnlyModern
    @Accordion
    var moongladeBeacon = MoongladeBeaconConfig()

    @Expose
    @ConfigOption(name = "Birries Highlight", desc = "")
    @OnlyModern
    @Accordion
    var birriesHighlight = BirriesHighlightConfig()

    @Expose
    @ConfigOption(
        name = "Leaf X-Ray",
        desc = "Makes leaves transparent to see through them when foraging."
    )
    @OnlyModern
    @ConfigEditorBoolean
    @FeatureToggle
    var leafXray: Boolean = true

    @Expose
    @ConfigOption(
        name = "Leaf X-Ray Cooldown",
        desc = "The time you have to wait after gaining foraging exp before the leaves become visible again."
    )
    @ConfigEditorSlider(minValue = 1F, maxValue = 60F, minStep = 1F)
    @OnlyModern
    var leafXrayCooldown: Float = 5F // in seconds, default is 5 seconds
}
