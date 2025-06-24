package at.hannibal2.skyhanni.config.features.mining.glacite

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.features.mining.glacitemineshaft.CorpseType
import at.hannibal2.skyhanni.features.mining.glacitemineshaft.DropSound
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class CorpseTrackerConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the Corpse Tracker overlay for Glacite Mineshafts.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Only when in Mineshaft", desc = "Only show the overlay while in a Glacite Mineshaft.")
    @ConfigEditorBoolean
    var onlyInMineshaft: Boolean = false

    @Expose
    @ConfigLink(owner = CorpseTrackerConfig::class, field = "enabled")
    val position: Position = Position(-274, 0)

    @Expose
    @ConfigOption(
        name = "Send webhook on Corpse Loot",
        desc = "Send a Discord webhook when you loot a Corpse in the Glacite Mineshaft.\n" +
            "Remember to set your webhook URL in the webhook settings!"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var sendWebhookOnCorpseLoot: Boolean = false

    @Expose
    @ConfigOption(
        name = "Corpse Webhook Types",
        desc = "The types of corpses to send webhooks for.\n" +
            "You can select multiple types."
    )
    @ConfigEditorDraggableList
    var corpseWebhookTypes: Property<MutableList<CorpseType>> = Property.of(
        mutableListOf(
            CorpseType.VANGUARD,
        )
    )

    @Expose
    @ConfigOption(
        name = "Locket Sound",
        desc = "Play a sound when you drop a Shattered Locket",
    )
    @ConfigEditorBoolean
    var playLocketSound: Boolean = true

    @Expose
    @ConfigOption(
        name = "Sound to play",
        desc = "The sound to play when you drop a Shattered Locket."
    )
    @ConfigEditorDropdown
    var dropSound: DropSound = DropSound.MINECRAFT_CHALLENGE_COMPLETED

    @ConfigOption(
        name = "Preview Sound",
        desc = "Preview the sound that will be played when you drop a Shattered Locket.",
    )
    @ConfigEditorButton(buttonText = "Preview")
    var previewSound: Runnable = Runnable {
        dropSound.playSound()
    }

    @Expose
    @ConfigOption(
        name = "Funny Lapis Thingy",
        desc = "Funny"
    )
    @ConfigEditorBoolean
    var funnyLapisThingy: Boolean = true

    @Expose
    @ConfigOption(
        name = "Secret Number",
        desc = "Shhhh"
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 1_500_000f, minStep = 1f)
    var secretNumber: Float = 1_000_000f
}
