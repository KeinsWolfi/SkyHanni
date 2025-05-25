package at.hannibal2.skyhanni.features.mining.glacitemineshaft

import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import net.minecraft.client.audio.ISound

enum class DropSound(val sound: ISound, val displayName: String) {
    GIGACHAD(
        sound = SoundUtils.dropSoundGigaChad,
        displayName = "GigaChad",
    ),
    GOOFY_LAUGH(
        sound = SoundUtils.dropSoundGoofyLaugh,
        displayName = "Goofy Laugh",
    ),
    INSANE(
        sound = SoundUtils.dropSoundInsane,
        displayName = "Insane",
    ),
    MINECRAFT_CHALLENGE_COMPLETED(
        sound = SoundUtils.dropSoundMinecraftChallengeCompleted,
        displayName = "Minecraft Challenge Completed",
    ),
    OH_MY_GOD(
        sound = SoundUtils.dropSoundOhMyGod,
        displayName = "Oh My God",
    ),
    SAD_TROMBONE(
        sound = SoundUtils.dropSoundSadTrombone,
        displayName = "Sad Trombone",
    ),
    ;

    fun playSound() {
        sound.playSound()
    }

    override fun toString(): String {
        return displayName
    }
}
