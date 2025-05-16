package at.hannibal2.skyhanni.features.mining.crystalhollows

import net.minecraft.util.BlockPos
import kotlin.math.max
import kotlin.math.min

/** A mutable cluster of glass blocks that make up one Fairy Grotto. */
class FairyGrottoCluster(initial: BlockPos) {

    val blocks = mutableSetOf(initial)

    var minX = initial.x
        private set
    var minY = initial.y
        private set
    var minZ = initial.z
        private set
    var maxX = initial.x
        private set
    var maxY = initial.y
        private set
    var maxZ = initial.z
        private set
    var center = initial
        private set

    /** True if the given pos is within `range` of this cluster’s bbox. */
    fun isNear(pos: BlockPos, range: Int): Boolean {
        val dx = when {
            pos.x < minX -> minX - pos.x
            pos.x > maxX -> pos.x - maxX
            else -> 0
        }
        val dy = when {
            pos.y < minY -> minY - pos.y
            pos.y > maxY -> pos.y - maxY
            else -> 0
        }
        val dz = when {
            pos.z < minZ -> minZ - pos.z
            pos.z > maxZ -> pos.z - maxZ
            else -> 0
        }
        return dx * dx + dy * dy + dz * dz <= range * range
    }

    /** Merge `other` into this cluster. */
    fun merge(other: FairyGrottoCluster) {
        blocks += other.blocks
        minX = min(minX, other.minX)
        minY = min(minY, other.minY)
        minZ = min(minZ, other.minZ)
        maxX = max(maxX, other.maxX)
        maxY = max(maxY, other.maxY)
        maxZ = max(maxZ, other.maxZ)
        updateCenter()
    }

    /** Add a new block and enlarge bbox if needed. */
    fun add(pos: BlockPos) {
        if (blocks.add(pos)) {
            minX = min(minX, pos.x)
            minY = min(minY, pos.y)
            minZ = min(minZ, pos.z)
            maxX = max(maxX, pos.x)
            maxY = max(maxY, pos.y)
            maxZ = max(maxZ, pos.z)
            updateCenter()
        }
    }

    private fun updateCenter() {
        center = BlockPos(
            (minX + maxX) / 2,
            (minY + maxY) / 2,
            (minZ + maxZ) / 2
        )
    }
}
