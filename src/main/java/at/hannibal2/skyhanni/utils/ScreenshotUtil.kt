package at.hannibal2.skyhanni.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import java.awt.image.BufferedImage
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

/**
 * Represents a screenshot stored on disk.
 *
 * Use `use { … }` or call [delete] when you’re done.
 */
data class TempImage(val path: Path) : Closeable {
    val fileName: String get() = path.fileName.toString()

    /** Deletes the file; returns *true* if it actually disappeared. */
    fun delete(): Boolean = Files.deleteIfExists(path)

    override fun close() { delete() }
}

object ScreenshotUtil {

    /**
     * Captures the current Minecraft framebuffer to a temporary PNG file
     * and returns a [TempImage] handle to it.
     */
    fun captureScreenshot(): TempImage {
        val mc = Minecraft.getMinecraft()
        val fb: Framebuffer = mc.framebuffer

        //#if MC < 1.21
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fb.framebufferObject)
        //#else
        //$$ val fboField = Framebuffer::class.java.getDeclaredField("framebufferObject")
        //$$ fboField.isAccessible = true
        //$$ GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboField.getInt(fb))
        //#endif

        val w = fb.framebufferWidth
        val h = fb.framebufferHeight
        val buf = ByteBuffer.allocateDirect(w * h * 4)

        GL11.glReadPixels(0, 0, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf)

        val img = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val i = (x + y * w) * 4
                val r = buf.get(i).toInt() and 0xFF
                val g = buf.get(i + 1).toInt() and 0xFF
                val b = buf.get(i + 2).toInt() and 0xFF
                img.setRGB(x, h - y - 1, (r shl 16) or (g shl 8) or b)
            }
        }
        buf.clear()

        val tmpPath = Files.createTempFile("skyhanni_screenshot_", ".png")
        ImageIO.write(img, "png", tmpPath.toFile())
        // in case the program crashes before delete(): OS cleans up on reboot
        tmpPath.toFile().deleteOnExit()

        return TempImage(tmpPath)
    }
}
