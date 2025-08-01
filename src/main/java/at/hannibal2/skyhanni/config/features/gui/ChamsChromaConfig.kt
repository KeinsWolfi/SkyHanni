package at.hannibal2.skyhanni.config.features.gui

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ChamsChromaConfig {
    @Expose
    @ConfigOption(
        name = "Color",
        desc = "Color of the Chams Chroma.",
    )
    @ConfigEditorColour
    var color: ChromaColour = ChromaColour(0f, 1f, 1f, 250, 127)
}
