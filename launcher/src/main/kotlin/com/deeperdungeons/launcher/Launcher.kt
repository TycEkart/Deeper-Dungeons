package com.deeperdungeons.launcher

import com.deeperdungeons.backend.Application
import org.springframework.boot.runApplication
import java.awt.BorderLayout
import java.awt.Desktop
import java.io.OutputStream
import java.io.PrintStream
import java.net.URI
import javax.swing.*
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    // Disable headless mode to allow AWT/Swing usage
    System.setProperty("java.awt.headless", "false")

    SwingUtilities.invokeLater {
        createAndShowGUI(args)
    }
}

fun createAndShowGUI(args: Array<String>) {
    val frame = JFrame("Deeper Dungeons Server")
    frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    frame.setSize(800, 600)
    frame.layout = BorderLayout()

    // Text area for logs
    val textArea = JTextArea()
    textArea.isEditable = false
    textArea.lineWrap = true
    textArea.wrapStyleWord = true
    val scrollPane = JScrollPane(textArea)
    // Auto-scroll
    scrollPane.verticalScrollBar.addAdjustmentListener { e ->
        if (!e.valueIsAdjusting) {
            e.adjustable.value = e.adjustable.maximum
        }
    }
    
    frame.add(scrollPane, BorderLayout.CENTER)

    // Redirect System.out and System.err
    val outStream = PrintStream(TextAreaOutputStream(textArea))
    System.setOut(outStream)
    System.setErr(outStream)

    // Center on screen
    frame.setLocationRelativeTo(null)
    frame.isVisible = true

    println("Starting Deeper Dungeons Server...")

    // Run Spring Boot in a separate thread to avoid freezing the GUI
    thread(isDaemon = false) {
        try {
            val context = runApplication<Application>(*args)
            
            val port = context.environment.getProperty("server.port", "8090")
            val url = "http://localhost:$port"
            
            println("Server started on port $port")
            println("Opening browser at $url")
            
            openBrowser(url)
        } catch (e: Exception) {
            e.printStackTrace()
            JOptionPane.showMessageDialog(frame, "Error starting server: ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
        }
    }
}

class TextAreaOutputStream(private val textArea: JTextArea) : OutputStream() {
    override fun write(b: Int) {
        updateText(b.toChar().toString())
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        updateText(String(b, off, len))
    }

    private fun updateText(text: String) {
        SwingUtilities.invokeLater {
            textArea.append(text)
        }
    }
}

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