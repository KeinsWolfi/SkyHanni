package at.hannibal2.skyhanni.config.features.foraging

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.OnlyLegacy
import at.hannibal2.skyhanni.config.OnlyModern
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.annotations.SearchTag

/**
 * Attention developers:
 * If your feature can only be used on the foraging islands please mark it with @[OnlyModern]
 */
class ForagingConfig {

    @ConfigOption(
        name = "§cNotice",
        desc = "To see all Foraging features, please launch the game on a modern version of Minecraft with SkyHanni installed.\n" +
            "§eJoin the SkyHanni Discord for a guide on how to migrate the config.",
    )
    @OnlyLegacy
    @ConfigEditorInfoText
    var notice: String = ""

    @Expose
    @Category(name = "HotF", desc = "Settings for Heart of the Forest.")
    val hotf: HotfConfig = HotfConfig()

    @Expose
    @Category(name = "Trees", desc = "Settings for big trees found on the foraging islands.")
    val trees: TreesConfig = TreesConfig()

    @Expose
    @ConfigOption(name = "Starlyn Contests", desc = "")
    @SearchTag("Agatha")
    @Accordion
    val starlynContest: StarlynContestsConfig = StarlynContestsConfig()

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
    var leafXrayCooldown: Float = 5F

    @Expose
    @ConfigOption(
        name = "Auto Reel",
        desc = "Automatically reels in mobs when they are ready to be reeled in."
    )
    @OnlyModern
    @ConfigEditorBoolean
    @FeatureToggle
    var autoReel: Boolean = false

    @Expose
    @ConfigOption(name = "Foraging Tracker", desc = "")
    @OnlyModern
    @Accordion
    val tracker = ForagingTrackerConfig()

    @Expose
    @ConfigOption(name = "Mute Phantoms", desc = "Silences Phantoms in the Galatea.")
    @ConfigEditorBoolean
    @FeatureToggle
    @OnlyModern
    var mutePhantoms = true
}
