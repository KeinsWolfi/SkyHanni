package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.hypixelapi.HypixelLocationApi
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ChunkLoadEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.ReflectionUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import net.minecraft.block.Block
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.IBlockState
import net.minecraft.crash.CrashReport
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.ReportedException
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.client.ClientCommandHandler
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


@SkyHanniModule
object CrystalNucleusStructureScanner {

    val config get() = SkyHanniMod.feature.mining.structureScannerConfig

    //#if MC < 1.21
    private val blocksToRemoveCoords = listOf(
        Triple(0, 0, -3),
        Triple(0, 1, -3),
        Triple(0, 2, -3),
        Triple(0, 3, -3),
        Triple(0, 4, -3),
    )

    private val blocksToRemove = mutableListOf<LorenzVec>()

    class World {
        val crystalWaypoints: ConcurrentHashMap<String, BlockPos> = ConcurrentHashMap()
        private val mobSpotWaypoints: ConcurrentHashMap<String, BlockPos> = ConcurrentHashMap()
        val fairyGrottos: CopyOnWriteArrayList<FairyGrottoCluster> = CopyOnWriteArrayList()
        private val dragonNestWaypoints: ConcurrentHashMap<BlockPos?, Int> = ConcurrentHashMap()
        val chunkCache: HashSet<Int> = HashSet()

        fun updateCrystalWaypoints(name: String, blockPos: BlockPos) {
            crystalWaypoints[name] = blockPos
        }

        fun updateMobSpotWaypoints(name: String, blockPos: BlockPos) {
            mobSpotWaypoints[name] = blockPos
        }

        fun updateFairyGrottos(pos: BlockPos) {
            // 32-block proximity rule
            val range = 32

            // Step 1 – find the first cluster close enough
            var host: FairyGrottoCluster? = null
            for (cluster in fairyGrottos) {
                if (cluster.isNear(pos, range)) {
                    host = cluster
                    break
                }
            }

            // Step 2 – none found? create a new one
            if (host == null) {
                host = FairyGrottoCluster(pos)
                fairyGrottos += host
            } else {
                host.add(pos)
            }

            // Step 3 – merge any *other* clusters that are now overlapping
            val toRemove = mutableListOf<FairyGrottoCluster>()
            for (cluster in fairyGrottos) {
                if (cluster !== host && cluster.isNear(pos, range)) {
                    host.merge(cluster)
                    toRemove += cluster
                }
            }
            fairyGrottos.removeAll(toRemove.toSet())
        }

        fun updateDragonNest(blockPos: BlockPos?) {
            dragonNestWaypoints[blockPos] = 0
        }

        fun cacheChunk(chunk: Chunk) {
            chunkCache.add(chunk.xPosition * 65536 + chunk.zPosition)
        }

        internal fun isChunkCached(chunk: Chunk): Boolean {
            return chunkCache.contains(chunk.xPosition * 65536 + chunk.zPosition)
        }
    }
    private var currentWorld: World = World()
    var cooldown: Int = 100
    private var initialScan: Boolean = false

    private var unloadedTimestamp: Long = 0

    private val internalSkytilsNames: HashMap<String?, String?> = hashMapOf(
        "§6King" to "internal_king",
        "§6Queen" to "internal_den",
        "§2Divan" to "internal_mines",
        "§5Temple" to "internal_temple",
        "§bCity" to "internal_city",
        "§4Bal" to "internal_bal"
    )

    @HandleEvent(onlyOnIsland = IslandType.CRYSTAL_HOLLOWS)
    fun onChunkLoad(event: ChunkLoadEvent) {
        if (!config.enabled) return
        if (cooldown != 0) return
        // ChatUtils.debug("Scanning chunk ${event.chunk.xPosition}, ${event.chunk.zPosition}")
        if (!currentWorld.isChunkCached(event.chunk)) {
            handleChunkLoad(event.chunk, currentWorld)
            currentWorld.cacheChunk(event.chunk)
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.CRYSTAL_HOLLOWS)
    fun onTick(event: SkyHanniTickEvent) {
        if (!config.enabled) return
        if (cooldown > 0) {
            cooldown--
        }
        if (cooldown == 1) {
            ChatUtils.debug("Creating new world for ${HypixelLocationApi.serverId}")
            currentWorld = World()
        }
        if (cooldown == 0) {
            for (coord in blocksToRemove) {
                MinecraftCompat.localWorld.setBlockToAir(
                    coord.toBlockPos()
                )
            }

            if (initialScan) return
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

    @Suppress("LongMethod")
    private fun handleChunkLoad(chunk: Chunk, currentWorld: World) {
        for (x in 0..15) {
            for (y in 0..169) {
                for (z in 0..15) {
                    for (structure in Structure.entries) {
                        if (structure.type == StructureType.CH_CRYSTALS) {
                            if (!currentWorld.crystalWaypoints.containsKey(structure.displayName)) {
                                if (structure != Structure.BAL || y < 80) {
                                    if (scanStructure(chunk, structure, x, y, z)) {
                                        sendCoordinatesMessage(
                                            structure,
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
                                        if (structure == Structure.TEMPLE) {
                                            StructureWaypoints.waypoints.add(
                                                StructureWaypoint(
                                                    displayName = "§5Temple Crystal",
                                                    onlyText = false,
                                                    location = LorenzVec(
                                                        chunk.xPosition * 16 + x,
                                                        y,
                                                        chunk.zPosition * 16 + z,
                                                    ),
                                                    color = Color(170, 0, 170),
                                                )
                                            )
                                            for (coord in blocksToRemoveCoords) {
                                                blocksToRemove.add(
                                                    LorenzVec(
                                                        chunk.xPosition * 16 + x + coord.first,
                                                        y + coord.second,
                                                        chunk.zPosition * 16 + z + coord.third
                                                    )
                                                )
                                            }
                                        }
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
                                sendCoordinatesMessage(
                                    structure,
                                    chunk.xPosition * 16 + x + structure.offsetX,
                                    y + structure.offsetY,
                                    chunk.zPosition * 16 + z + structure.offsetZ,
                                )
                                return
                            }
                        }

                        if (structure.type == StructureType.FAIRY_GROTTO) {
                            if (scanStructure(chunk, structure, x, y, z)) {
                                currentWorld.updateFairyGrottos(
                                    BlockPos(
                                        chunk.xPosition * 16 + x,
                                        y,
                                        chunk.zPosition * 16 + z
                                    )
                                )
                            }
                        }

                        if (structure.type == StructureType.GOLDEN_DRAGON) {
                            if (scanStructure(chunk, structure, x, y, z)) {
                                currentWorld.updateDragonNest(
                                    BlockPos(
                                        chunk.xPosition * 16 + x + structure.offsetX,
                                        y + structure.offsetY,
                                        chunk.zPosition * 16 + z + structure.offsetZ,
                                    ),
                                )
                                sendCoordinatesMessage(
                                    structure,
                                    chunk.xPosition * 16 + x + structure.offsetX,
                                    y + structure.offsetY,
                                    chunk.zPosition * 16 + z + structure.offsetZ,
                                )
                                return
                            }
                        }

                        if (structure.type == StructureType.CH_UNIQUE) {
                            if (scanStructure(chunk, structure, x, y, z)) {
                                currentWorld.updateDragonNest(
                                    BlockPos(
                                        chunk.xPosition * 16 + x + structure.offsetX,
                                        y + structure.offsetY,
                                        chunk.zPosition * 16 + z + structure.offsetZ,
                                    ),
                                )
                                sendCoordinatesMessage(
                                    structure,
                                    chunk.xPosition * 16 + x + structure.offsetX,
                                    y + structure.offsetY,
                                    chunk.zPosition * 16 + z + structure.offsetZ,
                                )
                                return
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendCoordinatesMessage(structure: Structure, x: Int, y: Int, z: Int) {
        val name = structure.displayName

        val builder = StringBuilder()

        builder.append(LorenzColor.YELLOW.getChatColor())
            .append("Found ")
            .append(name)
            .append(" " + LorenzColor.YELLOW.getChatColor())
            .append("at ")
            .append(x)
            .append(", ")
            .append(y)
            .append(", ")
            .append(z)

        ChatUtils.chat(builder.toString(), prefix = false)

        if (config.waypoints) {
            StructureWaypoints.waypoints.add(
                StructureWaypoint(
                    displayName = name,
                    onlyText = false,
                    location = LorenzVec(x, y, z),
                    color = structure.color,
                ),
            )
        }
    }

    private fun addToSkytilsMap(name: String, x: Int, y: Int, z: Int) {
        ClientCommandHandler.instance.executeCommand(
            MinecraftCompat.localPlayer,
            "/sthw set " + internalSkytilsNames[name] + " " + x + " " + y + " " + z,
        )
    }

    private fun scanStructure(chunk: Chunk, structure: Structure, x: Int, y: Int, z: Int): Boolean {
        if (!structure.quarter.testPredicate(BlockPos(chunk.xPosition * 16 + x, y, chunk.zPosition * 16 + z))) {
            return false
        }

        for (structureY in structure.blocks.indices) { // Use indices to ensure bounds safety
            val triple: Triple<Block, PropertyEnum<*>?, Comparable<*>?> = structure.blocks[structureY]

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
        var iBlockState = Blocks.air.defaultState
        if (y >= 0 && y shr 4 < chunk.blockStorageArray.size) {
            val extendedblockstorage = chunk.blockStorageArray[y shr 4]
            if (extendedblockstorage != null) {
                try {
                    iBlockState = extendedblockstorage[x, y and 0xF, z]
                } catch (throwable: Throwable) {
                    val crashReport = CrashReport.makeCrashReport(throwable, "Getting block")
                    throw ReportedException(crashReport)
                }
            }
        }
        return iBlockState
    }

    @HandleEvent
    fun onWorldUnload(event: WorldChangeEvent) {
        if (System.currentTimeMillis() - unloadedTimestamp > 2000) {
            cooldown = 80
            initialScan = false
            currentWorld = World()
            blocksToRemove.clear()
            unloadedTimestamp = System.currentTimeMillis()
        }
    }

    @HandleEvent
    fun onWorldRender(event: SkyHanniRenderWorldEvent) {
        if (!config.enabled) return

        for (cluster in currentWorld.fairyGrottos) {
            for (block in cluster.blocks) {
                if (block.y <= 64) {
                    event.drawWaypointFilled(
                        LorenzVec(block.x, block.y, block.z),
                        Color(255, 64, 64),
                        seeThroughBlocks = true,
                    )
                } else {
                    event.drawWaypointFilled(
                        LorenzVec(block.x, block.y, block.z),
                        Color(255, 85, 255),
                        seeThroughBlocks = true,
                    )
                }
            }

            val fieldBlocks = cluster.blocks.filter { it.y <= 64 }

            event.drawDynamicText(
                LorenzVec(cluster.center.x, cluster.center.y, cluster.center.z),
                "§dFairy Grotto: §c${fieldBlocks.size}§d/${cluster.blocks.size}",
                1.0,
            )
        }
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shrecheckstructures") {
            description = "Recheck Structures"
            category = CommandCategory.USERS_RESET
            callback {
                currentWorld.chunkCache.clear()
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
        event.register("ghostblock") {
            description = "Set Block youre looking at to air."
            category = CommandCategory.MAIN
            callback {
                val ray = MinecraftCompat.localPlayer.rayTrace(100.0, 1.0f)
                val blockState: IBlockState = MinecraftCompat.localWorld.getBlockState(ray.blockPos)
                val block: Block = blockState.block
                if (block != Blocks.air) {
                    blocksToRemove.add(
                        LorenzVec(
                            ray.blockPos.x,
                            ray.blockPos.y,
                            ray.blockPos.z
                        )
                    )
                    ChatUtils.chat("Set ${block.localizedName} at ${ray.blockPos.x}, ${ray.blockPos.y}, ${ray.blockPos.z} to air")
                } else {
                    ChatUtils.userError("Block is already air")
                }
            }
        }
    }
    //#endif
}
