package me.ducpro.minecontroller

import me.ducpro.minecontroller.controller.TestController
import me.ducpro.minecontroller.server.HttpServer
import org.bukkit.plugin.java.JavaPlugin

class MineController : JavaPlugin() {
    override fun onEnable() {
        val server = HttpServer(8080)
        server.mapControllers(listOf(TestController()))
        server.start()
    }

    override fun onDisable() {

    }
}