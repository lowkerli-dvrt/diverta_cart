package io.github.sonicarg.diverta_cart

import org.json.JSONObject
import org.json.JSONTokener
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.io.FileInputStream
import java.net.URI

val APP_LOGGER = LoggerFactory.getLogger("main")!!

@Volatile
var keepRunning = true

fun main() {
    APP_LOGGER.info("Starting application")

    APP_LOGGER.info("Gathering settings from 'config.json'")
    val config = JSONObject(JSONTokener(FileInputStream("config.json")))

    val port: Int = config.getJSONObject("server").getInt("port")
    APP_LOGGER.info("Starting server at port $port")
    CartServer.config = config
    CartServer.start()

    if (config.getJSONObject("ui").getBoolean("autoBrowserLaunch")) {
        APP_LOGGER.info("Starting default web browser as per config (ui/autoBrowserLaunch=true)")
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI("http://localhost:$port"))
        }
        else {
            APP_LOGGER.warn(
                "System does not support Java Desktop, therefore web browser cannot be launched automatically. " +
                "Please, use your preferred browser and navigate to 'http://localhost:$port'."
            )
        }
    }

    val mainThread = Thread.currentThread()
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            keepRunning = false
            mainThread.join()
        }
    })
}