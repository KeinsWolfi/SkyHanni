package at.hannibal2.skyhanni.config.features.misc.cosmetic

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class RotatingChestConfig {
    @Expose
    @ConfigOption(
        name = "Enable Rotating Chests",
        desc = "Enable funny rotating chests"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigOption(
        name = "ChestESP",
        desc = "Enable Chest ESP"
    )
    @ConfigEditorBoolean
    var chestESP: Boolean = true

    @Expose
    @ConfigOption(
        name = "Random Rotation",
        desc = "Random rotation of chests."
    )
    @ConfigEditorBoolean
    var randomRotation: Boolean = true

    @Expose
    @ConfigOption(
        name = "Horizontal Rotation",
        desc = "Velocity of horizontal rotation in degrees per second."
    )
    @ConfigEditorSlider(minValue = -360f, maxValue = 360f, minStep = 1f)
    var horizontalRotation: Float = 180f

    @Expose
    @ConfigOption(
        name = "Vertical Rotation",
        desc = "Velocity of vertical rotation in degrees per second."
    )
    @ConfigEditorSlider(minValue = -360f, maxValue = 360f, minStep = 1f)
    var verticalRotation: Float = 180f

    @Expose
    @ConfigOption(
        name = "Rotation Speed",
        desc = "Divides the speed by this value"
    )
    @ConfigEditorSlider(minValue = 1f, maxValue = 100f, minStep = 1f)
    var rotationSpeed: Float = 20f
}
