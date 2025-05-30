package at.hannibal2.skyhanni.config.features.mining

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.features.mining.pickobulus.PickobulusOverlayMode
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.input.Keyboard

class PickobulusDisplayConfig {
    @Expose
    @ConfigOption(
        name = "Pickobulus Display",
        desc = "This enables the main feature. Does not do anything yet."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigOption(
        name = "Show Overlay Keybind",
        desc = "This shows the keybind to toggle the Pickobulus overlay."
    )
    @ConfigEditorKeybind(
        defaultKey = Keyboard.KEY_F
    )
    var keyBindToggleOverlay: Int = Keyboard.KEY_F

    @Expose
    @ConfigOption(
        name = "Hold Keybind",
        desc = "Enabled: The overlay will only be shown while holding the keybind.\n" +
            "Disabled: The overlay will be toggled when pressing the key."
    )
    @ConfigEditorBoolean
    var holdKeybind: Boolean = true

    @Expose
    @ConfigOption(
        name = "Display Mode",
        desc = "The render mode of the Overlay"
    )
    @ConfigEditorDropdown
    var displayMode: PickobulusOverlayMode = PickobulusOverlayMode.ESP

    @Expose
    @ConfigOption(
        name = "Show Pity Gain",
        desc = "Show the Pity Gain in the Pickobulus overlay."
    )
    @ConfigEditorBoolean
    var showPityGain: Boolean = true

    @Expose
    @ConfigLink(owner = PickobulusDisplayConfig::class, field = "display")
    var displayPosition: Position = Position(120, 40)
}
