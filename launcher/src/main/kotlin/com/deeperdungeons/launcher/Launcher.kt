package com.deeperdungeons.launcher

import com.deeperdungeons.backend.Application
import org.springframework.boot.runApplication
import java.awt.BorderLayout
import java.io.PrintStream
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
            val port = findAvailablePort(8090)
            println("Found available port: $port")

            // Pass the port to Spring Boot
            val newArgs = args + arrayOf("--server.port=$port")
            val context = runApplication<Application>(*newArgs)
            
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
