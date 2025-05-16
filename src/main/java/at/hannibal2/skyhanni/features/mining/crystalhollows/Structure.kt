package at.hannibal2.skyhanni.features.mining.crystalhollows

import net.minecraft.block.Block
import net.minecraft.block.BlockColored
import net.minecraft.block.BlockStone
import net.minecraft.block.BlockStoneBrick
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import java.awt.Color

enum class Structure(
    val blocks: List<Triple<Block, PropertyEnum<*>?, Comparable<*>?>>,
    val type: StructureType,
    val quarter: CrystalHollowsQuarter,
    val displayName: String,
    val color: Color,
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
        Color(255, 170, 0),
        0, 5, 0,
    ),
    DIVAN(
        listOf(
            Triple(Blocks.quartz_block, null, null),
            Triple(Blocks.quartz_stairs, null, null),
            Triple(Blocks.stone_brick_stairs, null, null),
            Triple(Blocks.stonebrick, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.MITHRIL_DEPOSITS,
        "§2Divan",
        Color(0, 170, 0), // §2  → dark-green
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
        Color(85, 255, 255),
        24, 0, -17,
    ),
    TEMPLE(
        listOf(
            Triple(Blocks.bedrock, null, null),
            Triple(Blocks.bedrock, null, null),
            Triple(Blocks.bedrock, null, null),
            Triple(Blocks.bedrock, null, null),
            Triple(Blocks.stonebrick, BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CHISELED),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.JUNGLE,
        "§5Temple",
        Color(170, 0, 170),
        -19, 46, -39,
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
        Color(255, 170, 0),
        1, -1, 2,
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
        "§4Bal",
        Color(170, 0, 0),
        0, 1, 0,
    ),
    FAIRY_GROTTO(
        listOf(
            Triple(Blocks.stained_glass, BlockColored.COLOR, EnumDyeColor.MAGENTA)
        ),
        StructureType.FAIRY_GROTTO,
        CrystalHollowsQuarter.ANY,
        "",
        Color(255, 85, 255),
        0, 0, 0,
    ),
    GOLDEN_DRAGON(
        listOf(
            Triple(Blocks.stone, null, null),
            Triple(Blocks.stained_hardened_clay, BlockColored.COLOR, EnumDyeColor.RED),
            Triple(Blocks.stained_hardened_clay, BlockColored.COLOR, EnumDyeColor.RED),
            Triple(Blocks.stained_hardened_clay, BlockColored.COLOR, EnumDyeColor.RED),
            Triple(Blocks.skull, null, null),
            Triple(Blocks.wool, BlockColored.COLOR, EnumDyeColor.RED),
        ),
        StructureType.GOLDEN_DRAGON,
        CrystalHollowsQuarter.ANY,
        "", // no display name in original
        Color.ORANGE, // pick a visible highlight for the nest
        0, -3, 5,
    ),
    ODAWA(
        listOf(
            Triple(Blocks.spruce_fence, null, null),
            Triple(Blocks.spruce_fence, null, null),
            Triple(Blocks.spruce_fence, null, null),
            Triple(Blocks.hay_block, null, null),
        ),
        StructureType.CH_UNIQUE,
        CrystalHollowsQuarter.ANY,
        "§5Odawa",
        Color(255, 64, 255),
        -6, 1, 25,
    ),
    CORLEONE_BRIDGE(
        listOf(
            Triple(Blocks.cobblestone_wall, null, null),
            Triple(Blocks.iron_bars, null, null),
            Triple(Blocks.cobblestone_wall, null, null),
            Triple(Blocks.iron_bars, null, null),
            Triple(Blocks.cobblestone_wall, null, null),
            Triple(Blocks.iron_bars, null, null),
            Triple(Blocks.cobblestone_wall, null, null),
            Triple(Blocks.iron_bars, null, null),
            Triple(Blocks.cobblestone_wall, null, null),
            Triple(Blocks.iron_bars, null, null),
        ),
        StructureType.CH_MOB_SPOTS,
        CrystalHollowsQuarter.ANY,
        "§2Corleone Bridge",
        Color(0, 170, 0),
        20, -12, 25,
    )
}

enum class StructureType {
    FAIRY_GROTTO,
    CH_CRYSTALS,
    CH_MOB_SPOTS,
    WORM_FISHING,
    GOLDEN_DRAGON,
    CH_UNIQUE,
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
