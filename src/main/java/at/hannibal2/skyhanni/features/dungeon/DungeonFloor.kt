package at.hannibal2.skyhanni.features.dungeon

enum class DungeonFloor(
    private val bossName: String
) {
    E("The Watcher"),
    F1("Bonzo"),
    F2("Scarf"),
    F3("The Professor"),
    F4("Thorn"),
    F5("Livid"),
    F6("Sadan"),
    F7("Necron"),
    M1("Bonzo"),
    M2("Scarf"),
    M3("The Professor"),
    M4("Thorn"),
    M5("Livid"),
    M6("Sadan"),
    M7("Necron");

    companion object {

        fun byBossName(bossName: String) = DungeonFloor.entries.firstOrNull { it.bossName == bossName }
    }
}
