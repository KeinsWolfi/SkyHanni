package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import java.awt.SystemTray
import java.awt.TrayIcon

object SystemNotificationsUtils {
    fun showNotification(title: String, message: String) {
        if (!SystemTray.isSupported()) {
            ChatUtils.chat(
                "System tray is not supported on this system. Cannot display notification."
            )
            return
        }

        SkyHanniMod.trayIcon.displayMessage(
            title,
            message,
            TrayIcon.MessageType.WARNING
        )
    }
}
