package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.world.chunk.Chunk

class ChunkLoadEvent(val chunk: Chunk) : SkyHanniEvent()
