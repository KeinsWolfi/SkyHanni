package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getTitle
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.loreCosts
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.formatPercentage
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.collection.RenderableCollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.BucketedItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniBucketedItemTracker
import com.google.gson.annotations.Expose
import java.util.EnumMap

@SkyHanniModule
object DungeonProfitTracker {

    val repoGroup = RepoPattern.group("dungeon.tracker")

    /**
     * REGEX-TEST: Bedrock Chest
     * REGEX-TEST: Gold Chest
     * REGEX-TEST: Wood Chest
     * REGEX-TEST: Obsidian Chest
     * REGEX-TEST: Diamond Chest
     * REGEX-TEST: Emerald Chest
     */
    private val chestInventoryNamePattern by repoGroup.pattern(
        "chest.inventory.name",
        "(?<chestType>Wood|Gold|Emerald|Diamond|Obsidian|Bedrock) Chest",
    )

    /**
     * REGEX-TEST: §62,000,000 Coins
     * REGEX-TEST: §625,000 Coins
     */
    private val coinPattern by repoGroup.pattern(
        "chest.cost",
        "§6(?<cost>[\\d,]+) Coins",
    )

    //  §r§b§lDIAMOND CHEST REWARDS
    //    §r§5Hot Potato Book

    private val tracker = SkyHanniBucketedItemTracker(
        "Dungeon Profit Tracker",
        { BucketData() },
        { it.dungeonProfitTracker },
        { drawDisplay(it) },
    )

    class BucketData : BucketedItemTrackerData<DungeonFloor>() {
        override fun getCoinName(bucket: DungeonFloor?, item: TrackedItem) = "<no coins>"
        override fun getCoinDescription(bucket: DungeonFloor?, item: TrackedItem): List<String> = listOf("<no coins>")

        override fun DungeonFloor.isBucketSelectable(): Boolean = true

        override fun resetItems() {
            floorsDone.clear()
            coinsSpent.clear()
        }

        override fun getDescription(bucket: DungeonFloor?, timesGained: Long): List<String> {
            val floorsDoneNoneNull = floorsDone[bucket] ?: 0L
            val percentage = timesGained.toDouble() / floorsDoneNoneNull
            val dropRate = percentage.coerceAtMost(1.0).formatPercentage()
            return listOf(
                "§7Dropped §e${timesGained.addSeparators()} §7times.",
                "§7Your drop rate: §c$dropRate.",
            )
        }

        fun getFloorsDone(): Long {
            return if (selectedBucket == null || selectedBucket !in DungeonFloor.values()) {
                floorsDone.values.sum()
            } else {
                floorsDone[selectedBucket] ?: 0
            }
        }

        @Expose
        var floorsDone: MutableMap<DungeonFloor, Long> = EnumMap(DungeonFloor::class.java)

        @Expose
        var coinsSpent: MutableMap<DungeonFloor, Long> = EnumMap(DungeonFloor::class.java)
    }

    private fun drawDisplay(bucketData: BucketData): List<Searchable> = buildList {
        addSearchString("§b§lDragon Profit Tracker")
        tracker.addBucketSelector(this, bucketData, "Dungeon Floor")

        val profit = tracker.drawItems(bucketData, { true }, this)

        val colorCode = LorenzColor.DARK_RED
        val displayName = bucketData.selectedBucket?.name ?: "Total Dungeons"
        val dungeonsDone = bucketData.getFloorsDone()
        val dungeonString = "${colorCode.getChatColor()}$displayName §r§4Done: $dungeonsDone"
        add(
            Renderable.string(dungeonString).toSearchable(),
        )

        add(tracker.addTotalProfit(profit, bucketData.getFloorsDone(), "Dragon"))

        tracker.addPriceFromButton(this)
    }

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        val invName = event.inventoryName
        val inventoryItems = event.inventoryItems

        chestInventoryNamePattern.matchMatcher(invName) {
            val chestType = group("chestType")
            val openChestItem = inventoryItems[31] ?: return

            var chestCoinCost = 0

            for (line in openChestItem.getLore()) {
                coinPattern.matchMatcher(line) {
                    val cost = group("cost")
                    chestCoinCost = cost.formatInt()
                }
            }

            val chestCostOther = openChestItem.loreCosts()

            val chestCost = chestCoinCost + chestCostOther.sumOf { it.getPrice() }

            ChatUtils.debug(
                "Dungeon Chest Type: $chestType \n" +
                    "Chest Cost: $chestCost"
            )
        }
    }

    @HandleEvent
    fun onSloClicked(event: GuiContainerEvent.SlotClickEvent) {
        if (!chestInventoryNamePattern.matches(event.gui.getTitle()))
        if (event.slotId != 31) return


    }
}
