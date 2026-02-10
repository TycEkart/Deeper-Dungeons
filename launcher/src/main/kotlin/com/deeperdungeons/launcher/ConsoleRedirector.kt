package com.deeperdungeons.launcher

import java.io.OutputStream
import javax.swing.JTextArea
import javax.swing.SwingUtilities

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
