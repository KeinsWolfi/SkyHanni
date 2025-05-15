package at.hannibal2.skyhanni.config.features.mining.nucleus

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class StructureScannerConfig {
    @Expose
    @ConfigOption(
        name = "Enable Structure Scanner",
        desc = "Enable the structure scanner."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigOption(
        name = "Waypoints",
        desc = "Enable waypoints for the structure scanner."
    )
    @ConfigEditorBoolean
    var waypoints: Boolean = true
}
