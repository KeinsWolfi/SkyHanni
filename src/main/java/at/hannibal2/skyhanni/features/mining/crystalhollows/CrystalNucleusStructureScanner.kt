package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.hypixelapi.HypixelLocationApi
import at.hannibal2.skyhanni.events.ChunkLoadEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.ReflectionUtils
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import net.minecraft.block.Block
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.IBlockState
import net.minecraft.crash.CrashReport
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.ReportedException
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.storage.ExtendedBlockStorage
import net.minecraftforge.client.ClientCommandHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern


@SkyHanniModule
object CrystalNucleusStructureScanner {

    val config get() = SkyHanniMod.feature.mining.structureScannerConfig

    class World {
        val crystalWaypoints: ConcurrentHashMap<String, BlockPos> = ConcurrentHashMap()
        private val mobSpotWaypoints: ConcurrentHashMap<String, BlockPos> = ConcurrentHashMap()
        private val fairyGrottos: ConcurrentHashMap<BlockPos, Int> = ConcurrentHashMap()
        private val chunkCache: HashSet<Int> = HashSet()

        fun updateCrystalWaypoints(name: String, blockPos: BlockPos) {
            crystalWaypoints[name] = blockPos
        }

        fun updateMobSpotWaypoints(name: String, blockPos: BlockPos) {
            mobSpotWaypoints[name] = blockPos
        }

        fun updateFairyGrottos(blockPos: BlockPos) {
            fairyGrottos[blockPos] = 0
        }

        fun cacheChunk(chunk: Chunk) {
            chunkCache.add(chunk.xPosition * 65536 + chunk.zPosition)
        }

        internal fun isChunkCached(chunk: Chunk): Boolean {
            return chunkCache.contains(chunk.xPosition * 65536 + chunk.zPosition)
        }
    }
    val patternControlCode: Pattern = Pattern.compile("\\u00A7([0-9a-fk-or])", Pattern.CASE_INSENSITIVE)
    private val worlds: HashMap<String, World> = HashMap()
    var cooldown: Int = 100
    var initialScan: Boolean = false

    var unloadedTimestamp: Long = 0

    private val internalSkytilsNames: HashMap<String?, String?> = object : HashMap<String?, String?>() {
        init {
            put("§6King", "internal_king")
            put("§6Queen", "internal_den")
            put("§2Divan", "internal_mines")
            put("§5Temple", "internal_temple")
            put("§bCity", "internal_city")
            put("§6Bal", "internal_bal")
        }
    }

    @HandleEvent
    fun onChunkLoad(event: ChunkLoadEvent) {
        if (!config.enabled) return
        if (cooldown != 0) return
        val currentWorld = worlds[HypixelLocationApi.serverId] ?: return
        if (!currentWorld.isChunkCached(event.chunk)) {
            handleChunkLoad(event.chunk, currentWorld)
            currentWorld.cacheChunk(event.chunk)
        }
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!config.enabled) return
        if (cooldown > 0) {
            cooldown--
        }
        if (cooldown == 1 && !worlds.containsKey(HypixelLocationApi.serverId)) {
            worlds[HypixelLocationApi.serverId ?: "unknown"] = World()
        }
        if (cooldown == 0) {
            if (initialScan) return
            val currentWorld = worlds[HypixelLocationApi.serverId] ?: return
            initialScan = true
            val `object`: Any? = ReflectionUtils.field(MinecraftCompat.localWorld.chunkProvider, "field_73237_c")
            if (`object` is List<*>) {
                ChatUtils.debug("Scanning ${`object`.size} chunks")
                for (chunk in `object` as List<Chunk?>) {
                    currentWorld.cacheChunk(chunk!!)
                    handleChunkLoad(chunk, currentWorld)
                }
            }
        }
    }

    private fun handleChunkLoad(chunk: Chunk, currentWorld: World) {
        for (x in 0..15) {
            for (y in 0..169) {
                for (z in 0..15) {
                    for (structure in Structure.entries) {
                        if (structure.type == StructureType.CH_CRYSTALS) {
                            if (!currentWorld.crystalWaypoints.containsKey(structure.name)) {
                                if (structure != Structure.BAL || y < 80) {
                                    if (scanStructure(chunk, structure, x, y, z)) {
                                        sendCoordinatesMessage(
                                            structure.displayName,
                                            chunk.xPosition * 16 + x + structure.offsetX,
                                            y + structure.offsetY,
                                            chunk.zPosition * 16 + z + structure.offsetZ,
                                        )
                                        addToSkytilsMap(
                                            structure.displayName,
                                            chunk.xPosition * 16 + x + structure.offsetX,
                                            y + structure.offsetY,
                                            chunk.zPosition * 16 + z + structure.offsetZ,
                                        )
                                        currentWorld.updateCrystalWaypoints(
                                            structure.displayName,
                                            BlockPos(
                                                chunk.xPosition * 16 + x + structure.offsetX,
                                                y + structure.offsetY,
                                                chunk.zPosition * 16 + z + structure.offsetZ,
                                            ),
                                        )
                                        return
                                    }
                                }
                            }
                        }

                        if (structure.type == StructureType.CH_MOB_SPOTS) {
                            if (scanStructure(chunk, structure, x, y, z)) {
                                currentWorld.updateMobSpotWaypoints(
                                    structure.displayName,
                                    BlockPos(
                                        chunk.xPosition * 16 + x + structure.offsetX,
                                        y + structure.offsetY,
                                        chunk.zPosition * 16 + z + structure.offsetZ,
                                    ),
                                )
                                return
                            }
                        }

                        if (structure.type == StructureType.FAIRY_GROTTO) {
                            if (scanStructure(chunk, structure, x, y, z)) {
                                currentWorld.updateFairyGrottos(BlockPos(chunk.xPosition * 16 + x, y, chunk.zPosition * 16 + z))
                                return
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendCoordinatesMessage(name: String, x: Int, y: Int, z: Int) {
        val builder = StringBuilder()

        builder.append(LorenzColor.YELLOW.getChatColor())
            .append("Found a ")
            .append(name)
            .append(" " + LorenzColor.YELLOW.getChatColor())
            .append("at ")
            .append(x)
            .append(", ")
            .append(y)
            .append(", ")
            .append(z)

        ChatUtils.chat(builder.toString(), prefix = false)
    }

    private fun addToSkytilsMap(name: String, x: Int, y: Int, z: Int) {
        ClientCommandHandler.instance.executeCommand(
            MinecraftCompat.localPlayer,
            "/sthw set " + x + " " + y + " " + z + " " + internalSkytilsNames[name],
        )
    }

    private fun scanStructure(chunk: Chunk, structure: Structure, x: Int, y: Int, z: Int): Boolean {
        if (!structure.quarter.testPredicate(BlockPos(chunk.xPosition * 16 + x, y, chunk.zPosition * 16 + z))) {
            return false
        }

        for (structureY in 0 until structure.blocks.size) {
            val triple: Triple<Block, PropertyEnum<*>?, Comparable<*>?> = structure.blocks.get(structureY)

            if (triple.first != chunk.getBlock(x, y + structureY, z)) {
                return false
            }

            if (triple.second != null && triple.third != null && getBlockState(
                    chunk,
                    x,
                    y + structureY,
                    z,
                ).getValue(triple.second) !== triple.third
            ) {
                return false
            }
        }

        return true
    }

    private fun getBlockState(chunk: Chunk, x: Int, y: Int, z: Int): IBlockState {
        var extendedblockstorage: ExtendedBlockStorage = chunk.blockStorageArray[y shr 4]
        var iBlockState = Blocks.air.defaultState
        if (
            (y >= 0 && y shr 4 < chunk.blockStorageArray.size) && (
                chunk.blockStorageArray[y shr 4].also {
                    extendedblockstorage = it
                }
                ) != null
        ) {
            try {
                iBlockState = extendedblockstorage[x, y and 0xF, z]
            } catch (throwable: Throwable) {
                val crashReport = CrashReport.makeCrashReport(throwable, "Getting block")
                throw ReportedException(crashReport)
            }
        }
        return iBlockState
    }

    @HandleEvent
    fun onWorldUnload(event: WorldChangeEvent) {
        if (System.currentTimeMillis() - unloadedTimestamp > 2000) {
            cooldown = 80
            initialScan = false
            unloadedTimestamp = System.currentTimeMillis()
        }
    }
}
