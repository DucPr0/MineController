package me.ducpro.minecontroller.server

import me.ducpro.minecontroller.controller.BaseController
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler

class HttpServer(private val port: Int) {
    private val server = Server(this.port)
    val handler = ServletHandler()

    init {
        this.server.handler = handler
    }

    fun mapControllers(vararg controllers: BaseController) {
        controllers.forEach { controller -> controller.map(this.handler) }
    }

    fun start() {
        this.server.start()
    }

//    TODO: Add logic for stopping the server
    fun stop() {
        this.server.stop()
    }
}