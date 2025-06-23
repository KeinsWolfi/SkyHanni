package at.hannibal2.skyhanni.features.mining.pickobulus

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzVec
//#if MC < 1.21
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBox
//#else
//$$ import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.drawFilledBoundingBox
//$$ import at.hannibal2.skyhanni.utils.toLorenzVec
//$$ import net.minecraft.block.BlockState
//$$ import net.minecraft.block.Blocks
//#endif
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.block.Block
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin

@SkyHanniModule
object PickobulusOverlay {
    private val config get() = SkyHanniMod.feature.mining.pickobulusDisplayConfig

    private var enabled = false

    private val throughBlocks = listOf(0, 160, 31, 37, 38, 175, 9, 11)
    private val breakableBlock: List<List<Pair<Int, Int>>> = listOf(
        listOf(174 to 0, 95 to -1, 160 to -1),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList()
    )
    private val convertIntoBedrock: List<List<Pair<Int, Int>>> = listOf(
        listOf(
            97 to 0, 97 to 1, 159 to 9, 159 to 12, 172 to 0, 181 to 8, 168 to 0, 168 to 1, 168 to 2,
            82 to 0, 35 to 3, 35 to 7, 35 to 8, 1 to 4
        ),
        listOf(
            1 to 0, 4 to 0, 1 to 4, 168 to 0, 168 to 1, 168 to 2, 159 to 9, 35 to 3, 35 to 7, 22 to 0,
            41 to 0, 42 to 0, 57 to 0, 133 to 0, 152 to 0, 173 to 0, 14 to 0, 15 to 0, 16 to 0, 21 to 0,
            73 to 0, 56 to 0, 129 to 0, 153 to 0, 87 to 0, 89 to 0, 49 to 0, 121 to 0, 95 to 0, 160 to 0
        ),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList()
    )

    private var blockCoord: BlockPos? = null
    private var predicted = emptyList<BlockPos>()
    val counts = MutableList(18) {
        0
    } // total /1  pity / 2 hardstone / ice / tungsten / 5 umber / 6 mithril / titanium / gemstone / mithril powder / 10

    fun atanAOverB(a: Double, b: Double): Double? = if (a == 0.0 && b == 0.0) null else atan2(a, b)

    private fun angleToUnitVecXYZ(yaw: Float, pitch: Float): Vec3 {
        val yawRad = Math.toRadians(yaw.toDouble())
        val pitchRad = Math.toRadians(pitch.toDouble())
        return Vec3(
            -cos(pitchRad) * sin(yawRad),
            -sin(pitchRad),
            cos(pitchRad) * cos(yawRad)
        )
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config.enabled) return

        if (config.holdKeybind) {
            enabled = SkyHanniMod.feature.mining.pickobulusDisplayConfig.keyBindToggleOverlay.isKeyHeld()
        } else {
            if (config.keyBindToggleOverlay.isKeyClicked()) {
                enabled = !enabled
            }
        }

        if (!enabled) return

        blockCoord = getLookBlock(
            Vec3(
                MinecraftCompat.localPlayer.posX,
                MinecraftCompat.localPlayer.posY + MinecraftCompat.localPlayer.eyeHeight + 0.53625,
                MinecraftCompat.localPlayer.posZ
            ),
            angleToUnitVecXYZ(
                MinecraftCompat.localPlayer.rotationYaw,
                MinecraftCompat.localPlayer.rotationPitch
            )
        )

        if (config.displayMode == PickobulusOverlayMode.NONE) return

        when (config.displayMode) {
            PickobulusOverlayMode.ESP -> {
                predicted.forEach {
                    event.drawWaypointFilled(
                        LorenzVec(it.x, it.y, it.z),
                        Color.GRAY,
                        true,
                        extraSize = -0.3,
                    )
                }
            }
            else -> {
                predicted.forEach {
                    //#if MC < 1.21
                    event.drawFilledBoundingBox(
                        AxisAlignedBB(
                            it.add(0.3, 0.3, 0.3),
                            it.add(0.7, 0.7, 0.7)
                        ),
                        Color.GRAY,
                        0.5f,
                    )
                    //#else
                    //$$ event.drawFilledBoundingBox(
                    //$$    Box(
                    //$$        it.toLorenzVec().add(0.3, 0.3, 0.3).toVec3(),
                    //$$        it.toLorenzVec().add(0.7, 0.7, 0.7).toVec3()
                    //$$    ),
                    //$$    Color.GRAY,
                    //$$    0.5f,
                    //$$    seeThroughBlocks = true
                    //$$ )
                    //#endif
                }
            }
        }
    }

    /**
     * @HandleEvent
     * fun onKeyUp(event: KeyUpEvent) {
     *     if (!config.enabled) return
     *     if (event.keyCode != config.keyBindToggleOverlay) return
     *     if (config.holdKeybind) {
     *         enabled = false
     *     }
     * }
     */

    /**
     * @HandleEvent
     * fun onKeyDown(event: KeyDownEvent) {
     *    if (!config.enabled) return
     *    if (event.keyCode != config.keyBindToggleOverlay) return
     *    enabled = if (!config.holdKeybind) {
     *        !enabled
     *    } else {
     *        true
     *    }
     * }
     */
    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!enabled || !config.enabled) return
        predicted = predictPicko(blockCoord?.x ?: 0, blockCoord?.y ?: 0, blockCoord?.z ?: 0)
    }


    @HandleEvent
    fun onRender(event: GuiRenderEvent) {
        if (!enabled || !config.enabled || !config.showPityGain) return

        val stringList = mutableListOf<String>()

        for (i in 1 until counts.size) {
            val count = counts[i]
            if (count > 0) {
                when (i) {
                    1 -> stringList.add("Pity: $count")
                    2 -> stringList.add("Hardstone: $count")
                    3 -> stringList.add("Ice: $count")
                    4 -> stringList.add("Tungsten: $count")
                    5 -> stringList.add("Umber: $count")
                    6 -> stringList.add("Mithril: $count")
                    7 -> stringList.add("Titanium: $count")
                    8 -> stringList.add("Gemstone: $count")
                    9 -> stringList.add("Mithril Powder: $count")
                }
            }
        }

        config.displayPosition.renderRenderables(display(stringList), posLabel = "Pickobulus Overlay")
    }

    private fun display(list: List<String>) = listOf(
        Renderable.hoverTips(
            "§7Pity: §c${counts[1]}",
            list
        )
    )

    private fun getLookBlock(startPos: Vec3, lookUnitVec: Vec3, maxDistance: Int = 20): BlockPos? {
        val world = MinecraftCompat.localWorld

        // Distance steps in x/y/z
        val lengthOfSteps = listOf(
            floor(maxDistance * abs(lookUnitVec.xCoord) + 1).toInt(),
            floor(maxDistance * abs(lookUnitVec.yCoord) + 1).toInt(),
            floor(maxDistance * abs(lookUnitVec.zCoord) + 1).toInt()
        )

        val decimals = listOf(
            startPos.xCoord - floor(startPos.xCoord),
            startPos.yCoord - floor(startPos.yCoord),
            startPos.zCoord - floor(startPos.zCoord)
        )

        val steps = (0..2).map { n ->
            val vecComponent = listOf(lookUnitVec.xCoord, lookUnitVec.yCoord, lookUnitVec.zCoord)[n]
            val dec = decimals[n]
            val count = lengthOfSteps[n]
            (0 until count).map { i ->
                if (vecComponent > 0) (i + 1 - dec) / vecComponent else (-i - dec) / vecComponent
            } + Double.POSITIVE_INFINITY
        }

        val indexes = intArrayOf(0, 0, 0)
        val porN = DoubleArray(3) {
            val c = listOf(lookUnitVec.xCoord, lookUnitVec.yCoord, lookUnitVec.zCoord)[it]
            if (c > 0) 1.0 else -1.0
        }

        val base = BlockPos(floor(startPos.xCoord).toInt(), floor(startPos.yCoord).toInt(), floor(startPos.zCoord).toInt())

        var result: BlockPos? = null

        while (true) {
            val pos = BlockPos(
                base.x + (indexes[0] * porN[0]).toInt(),
                base.y + (indexes[1] * porN[1]).toInt(),
                base.z + (indexes[2] * porN[2]).toInt()
            )

            //#if MC < 1.21
            val block = Block.getIdFromBlock(world.getBlockState(pos).block)
            //#else
            //$$ val block = Block.getRawIdFromState(world.getBlockState(pos))
            //#endif

            if (block !in throughBlocks) {
                result = pos
                break
            }

            val minIndex = (0..2).minByOrNull { steps[it][indexes[it]] } ?: 0
            indexes[minIndex]++

            if (indexes[minIndex] >= steps[minIndex].size) break

            if (
                (
                    indexes[0].toDouble().pow(2) +
                        indexes[1].toDouble().pow(2) +
                        indexes[2].toDouble().pow(2)
                    ) >= maxDistance.toDouble().pow(2)
            ) break
        }

        return result
    }

    private fun predictPicko(x: Int, y: Int, z: Int): List<BlockPos> {
        val mode = 0
        if (mode == 5) return emptyList()

        counts.fill(0)
        val breakList = mutableListOf<BlockPos>()
        val (worldCopy, metadatas) = initializeWorldCopyAndMetadata(x, y, z)

        for (i in 1..6) {
            for (j in 1..6) {
                for (k in 1..6) {
                    if (isBreakable(worldCopy, i, j, k)) {
                        processBlock(
                            x, y, z, i, j, k, mode, worldCopy, metadatas, breakList
                        )
                    }
                }
            }
        }

        return breakList
    }

    //#if MC < 1.21
    private fun initializeWorldCopyAndMetadata(
        x: Int, y: Int, z: Int
    ): Pair<Array<Array<IntArray>>, Array<Array<IntArray>>> {
    //#else
    //$$ private fun initializeWorldCopyAndMetadata(
    //$$     x: Int, y: Int, z: Int
    //$$ ): Pair<Array<Array<IntArray>>, Array<Array<Array<BlockState>>>> {
    //#endif

        val worldCopy = Array(8) { Array(8) { IntArray(8) } }
        //#if MC < 1.21
        val metadatas = Array(8) { Array(8) { IntArray(8) } }
        //#else
        //$$ val metadatas = Array(8) { Array(8) { Array(8) { Blocks.AIR.defaultState} } }
        //#endif

        repeat(8) { i ->
            repeat(8) { j ->
                repeat(8) { k ->
                    val blockPos = BlockPos(x + i - 4, y + j - 4, z + k - 4)
                    val block = MinecraftCompat.localWorld.getBlockState(blockPos)
                    //#if MC < 1.21
                    worldCopy[i][j][k] = Block.getIdFromBlock(block.block)
                    metadatas[i][j][k] = block.block.getMetaFromState(block)
                    //#else
                    //$$ worldCopy[i][j][k] = Block.getRawIdFromState(block)
                    //$$ metadatas[i][j][k] = block
                    //#endif
                }
            }
        }

        return Pair(worldCopy, metadatas)
    }

    private fun isBreakable(
        worldCopy: Array<Array<IntArray>>, i: Int, j: Int, k: Int
    ): Boolean {
        return worldCopy[i - 1][j][k] == 0 ||
            worldCopy[i + 1][j][k] == 0 ||
            worldCopy[i][j - 1][k] == 0 ||
            worldCopy[i][j + 1][k] == 0 ||
            worldCopy[i][j][k - 1] == 0 ||
            worldCopy[i][j][k + 1] == 0
    }

    private fun processBlock(
        x: Int, y: Int, z: Int, i: Int, j: Int, k: Int, mode: Int,
        worldCopy: Array<Array<IntArray>>,
        //#if MC < 1.21
        metadatas: Array<Array<IntArray>>,
        //#else
        //$$ metadatas: Array<Array<Array<BlockState>>>,
        //#endif
        breakList: MutableList<BlockPos>
    ) {
        val blockId = worldCopy[i][j][k]
        val metadata = metadatas[i][j][k]

        when (mode) {
            //#if MC < 1.21
            0 -> processModeZero(blockId, metadata)
            2 -> if (blockId == 1 && metadata == 0) counts[2] += 1
            3 -> processModeThree(blockId, metadata)
            //#endif
        }

        if (mode >= 2) {
            if (blockId != 0 && blockId != 7) {
                breakList.add(BlockPos(x + i - 4, y + j - 4, z + k - 4))
                worldCopy[i][j][k] = 0
                counts[0] += 1
            }
        } else {
            //#if MC < 1.21
            handleBlockLogic(blockId, metadata, mode, x, y, z, i, j, k, breakList, worldCopy)
            //#endif
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun processModeZero(blockId: Int, metadata: Int) {
        when {
            blockId == 174 && metadata == 0 -> {
                counts[1] += 2
                counts[3] += 1
            }
            blockId in listOf(95, 160) -> {
                counts[1] += 2
                counts[8] += 1
            }
            blockId == 1 && metadata == 4 -> {
                counts[1] += 4
                counts[7] += 1
            }
            blockId == 97 && metadata == 0 -> counts[2] += 1
            blockId == 35 && metadata == 8 -> counts[2] += 1
            blockId == 168 && metadata in 0..2 -> {
                counts[1] += 1
                counts[6] += 1
                counts[9] += 3
            }
            blockId == 35 && metadata == 3 -> {
                counts[1] += 1
                counts[6] += 1
                counts[9] += 5
            }
            blockId == 35 && metadata == 7 -> {
                counts[1] += 1
                counts[6] += 1
                counts[9] += 1
            }
            blockId == 159 && metadata == 9 -> {
                counts[1] += 1
                counts[6] += 1
                counts[9] += 1
            }
            blockId == 159 && metadata == 12 -> {
                counts[1] += 2
                counts[5] += 1
            }
            blockId == 172 && metadata == 0 -> {
                counts[1] += 2
                counts[5] += 1
            }
            blockId == 181 && metadata == 8 -> {
                counts[1] += 2
                counts[5] += 1
            }
            blockId == 97 && metadata == 1 -> {
                counts[1] += 2
                counts[4] += 1
            }
            blockId == 82 && metadata == 0 -> {
                counts[1] += 2
                counts[4] += 1
            }
        }
    }

    private fun processModeThree(blockId: Int, metadata: Int) {
        when {
            blockId in listOf(95, 160) -> counts[8] += 1
            blockId == 168 && metadata in 0..2 -> counts[9] += 3
            blockId == 35 && metadata == 3 -> counts[9] += 5
            blockId == 35 && metadata == 7 -> counts[9] += 1
            blockId == 159 && metadata == 9 -> counts[9] += 1
        }
    }

    private fun handleBlockLogic(
        blockId: Int,
        metadata: Int,
        mode: Int,
        x: Int,
        y: Int,
        z: Int,
        i: Int,
        j: Int,
        k: Int,
        breakList: MutableList<BlockPos>,
        worldCopy: Array<Array<IntArray>>
    ) {
        breakableBlock[mode]?.forEach { (id, meta) ->
            if (blockId == id && (metadata == meta || meta == -1)) {
                breakList.add(BlockPos(x + i - 4, y + j - 4, z + k - 4))
                worldCopy[i][j][k] = 0
                counts[0] += 1
            }
        }
        convertIntoBedrock[mode]?.forEach { (id, meta) ->
            if (blockId == id && (metadata == meta || meta == -1)) {
                breakList.add(BlockPos(x + i - 4, y + j - 4, z + k - 4))
                worldCopy[i][j][k] = 7
                counts[0] += 1
            }
        }
    }
}
