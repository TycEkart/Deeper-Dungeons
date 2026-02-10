package com.deeperdungeons.launcher

import com.deeperdungeons.backend.Application
import org.springframework.boot.runApplication
import java.awt.Desktop
import java.net.URI

fun main(args: Array<String>) {
    // Disable headless mode to allow AWT Desktop usage
    System.setProperty("java.awt.headless", "false")

    val context = runApplication<Application>(*args)
    
    val port = context.environment.getProperty("server.port", "8090")
    val url = "http://localhost:$port"
    
    // Open browser in a separate thread to not block anything, though main is done here.
    Thread {
        try {
            openBrowser(url)
        } catch (e: Exception) {
            println("Failed to launch browser: ${e.message}")
        }
    }.start()
}

fun openBrowser(url: String) {
    val os = System.getProperty("os.name").lowercase()
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
}