package com.deeperdungeons.launcher

import java.net.ServerSocket

fun findAvailablePort(startPort: Int): Int {
    for (port in startPort..65535) {
        try {
            ServerSocket(port).use { return port }
        } catch (e: Exception) {
            // Port is likely in use, try the next one
        }
    }
    throw IllegalStateException("No available ports found starting from $startPort")
}
