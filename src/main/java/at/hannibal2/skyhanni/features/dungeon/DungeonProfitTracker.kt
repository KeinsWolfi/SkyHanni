package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.TestExportTools
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.loreCosts
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack

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

    //  §r§b§lDIAMOND CHEST REWARDS
    //    §r§5Hot Potato Book
    //  §r§b§lDIAMOND CHEST REWARDS

    // internalName:NONE
    // display name: '§aOpen Reward Chest'
    // minecraft id: 'minecraft:chest'
    // lore:
    //  '§7Purchase this chest to receive the'
    //  '§7rewards above. You can only open'
    //  '§7one chest per Dungeons run -'
    //  '§7choose wisely!'
    //  ''
    //  '§7Cost'
    //  '§625,000 Coins'
    //  '§9Dungeon Chest Key'
    //  ''
    //  '§7§cNOTE: Coins are withdrawn from your'
    //  '§cbank if you don't have enough in'
    //  '§cyour purse.'
    //
    // getTagCompound
    //   display:
    //     Name: "§aOpen Reward Chest"

    // internalName:NONE
    // display name: '§aOpen Reward Chest'
    // minecraft id: 'minecraft:chest'
    // lore:
    //  '§7Purchase this chest to receive the'
    //  '§7rewards above. You can only open'
    //  '§7one chest per Dungeons run -'
    //  '§7choose wisely!'
    //  ''
    //  '§7Cost'
    //  '§9Dungeon Chest Key'
    //
    // getTagCompound
    //   display:
    //     Name: "§aOpen Reward Chest"

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        val invName = event.inventoryName
        val inventoryItems = event.inventoryItems

        chestInventoryNamePattern.matchMatcher(invName) {
            val chestType = group("chestType")
            val openChestItem = inventoryItems[31] ?: return

            val chestCost = openChestItem.loreCosts()

            ChatUtils.debug(
                "Dungeon Chest Type: $chestType \n" +
                    "Chest Cost: $chestCost"
            )
        }
    }

    // internalName:NONE
    // display name: '§aOpen Reward Chest'
    // minecraft id: 'minecraft:chest'
    // slot: 31
    // lore:
    //  '§7Purchase this chest to receive the'
    //  '§7rewards above. You can only open'
    //  '§7one chest per Dungeons run -'
    //  '§7choose wisely!'
    //  ''
    //  '§7Cost'
    //  '§aFREE'
    //
    // getTagCompound
    //   display:
    //     Name: "§aOpen Reward Chest"

    // internalName:NONE
    // display name: '§aOpen Reward Chest'
    // minecraft id: 'minecraft:chest'
    // slot: 31
    // lore:
    //  '§7Purchase this chest to receive the'
    //  '§7rewards above. You can only open'
    //  '§7one chest per Dungeons run -'
    //  '§7choose wisely!'
    //  ''
    //  '§7Cost'
    //  '§62,000,000 Coins'
    //  ''
    //  '§7§cNOTE: Coins are withdrawn from your'
    //  '§cbank if you don't have enough in'
    //  '§cyour purse.'
    //
    // getTagCompound
    //   display:
    //     Name: "§aOpen Reward Chest"
}
