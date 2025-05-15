package at.hannibal2.skyhanni.features.mining.crystalhollows

import net.minecraft.block.Block
import net.minecraft.block.BlockColored
import net.minecraft.block.BlockStone
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos

enum class Structure(
    val blocks: List<Triple<Block, PropertyEnum<*>?, Comparable<*>?>>,
    val type: StructureType,
    val quarter: CrystalHollowsQuarter,
    val displayName: String,
    val offsetX: Int,
    val offsetY: Int,
    val offsetZ: Int,
) {
    QUEEN(
        listOf(
            Triple(Blocks.stone, null, null),
            Triple(Blocks.log2, null, null),
            Triple(Blocks.log2, null, null),
            Triple(Blocks.log2, null, null),
            Triple(Blocks.log2, null, null),
            Triple(Blocks.cauldron, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.GOBLIN_HOLDOUT,
        "§6Queen",
        0, 5, 0,
    ),
    CITY(
        listOf(
            Triple(Blocks.cobblestone, null, null),
            Triple(Blocks.cobblestone, null, null),
            Triple(Blocks.cobblestone, null, null),
            Triple(Blocks.cobblestone, null, null),
            Triple(Blocks.stone_stairs, null, null),
            Triple(Blocks.stone, BlockStone.VARIANT, BlockStone.EnumType.ANDESITE_SMOOTH),
            Triple(Blocks.stone, BlockStone.VARIANT, BlockStone.EnumType.ANDESITE_SMOOTH),
            Triple(Blocks.dark_oak_stairs, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.PRECURSOR_REMNANTS,
        "§bCity",
        24, 0, -17
    ),
    TEMPLE(
        listOf(
            Triple(Blocks.bedrock, null, null),
            Triple(Blocks.clay, null, null),
            Triple(Blocks.clay, null, null),
            Triple(Blocks.stained_hardened_clay, null, null),
            Triple(Blocks.wool, null, null),
            Triple(Blocks.leaves, null, null),
            Triple(Blocks.leaves, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.JUNGLE,
        "§5Temple",
        -45, 47, -18
    ),
    KING(
        listOf(
            Triple(Blocks.wool, null, null),
            Triple(Blocks.dark_oak_stairs, null, null),
            Triple(Blocks.dark_oak_stairs, null, null),
            Triple(Blocks.dark_oak_stairs, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.GOBLIN_HOLDOUT,
        "§6King",
        1, -1, 2
    ),
    BAL(
        listOf(
            Triple(Blocks.lava, null, null),
            Triple(Blocks.barrier, null, null),
            Triple(Blocks.barrier, null, null),
            Triple(Blocks.barrier, null, null),
            Triple(Blocks.barrier, null, null),
            Triple(Blocks.barrier, null, null),
            Triple(Blocks.barrier, null, null),
            Triple(Blocks.barrier, null, null),
            Triple(Blocks.barrier, null, null),
            Triple(Blocks.barrier, null, null),
            Triple(Blocks.barrier, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.MAGMA_FIELDS,
        "§6Bal",
        0, 1, 0
    ),
    FAIRY_GROTTO(
        listOf(
            Triple(Blocks.stained_glass, BlockColored.COLOR, EnumDyeColor.MAGENTA)
        ),
        StructureType.FAIRY_GROTTO,
        CrystalHollowsQuarter.ANY,
        "",
        0, 0, 0
    )
}

enum class StructureType {
    FAIRY_GROTTO,
    CH_CRYSTALS,
    CH_MOB_SPOTS,
    WORM_FISHING,
    GOLDEN_DRAGON
}

enum class CrystalHollowsQuarter(private val predicate: (BlockPos) -> Boolean) {
    // Expand by 4 chunks to make sure we don't miss any structures
    JUNGLE({ it.x <= 576 && it.z <= 576 }),
    PRECURSOR_REMNANTS({ it.x > 448 && it.z > 448 }),
    GOBLIN_HOLDOUT({ it.x <= 576 && it.z > 448 }),
    MITHRIL_DEPOSITS({ it.x > 448 && it.z <= 576 }),
    MAGMA_FIELDS({ it.y < 80 }),
    ANY({ true });

    fun testPredicate(blockPos: BlockPos): Boolean = predicate(blockPos)
}
