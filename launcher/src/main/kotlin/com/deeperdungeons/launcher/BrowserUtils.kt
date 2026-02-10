package com.deeperdungeons.launcher

import java.awt.Desktop
import java.net.URI

fun openBrowser(url: String) {
    val os = System.getProperty("os.name").lowercase()
    try {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        } else {
            val runtime = Runtime.getRuntime()
            if (os.contains("win")) {
                runtime.exec(arrayOf("rundll32", "url.dll,FileProtocolHandler", url))
            } else if (os.contains("mac")) {
                runtime.exec(arrayOf("open", url))
            } else if (os.contains("nix") || os.contains("nux")) {
                runtime.exec(arrayOf("xdg-open", url))
            } else {
                println("Cannot open browser automatically on this platform.")
            }
        }
    } catch (e: Exception) {
        println("Failed to open browser: ${e.message}")
    }
}
