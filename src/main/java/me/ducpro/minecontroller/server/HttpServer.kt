package me.ducpro.minecontroller.server

import me.ducpro.minecontroller.controller.BaseController
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletHandler

class HttpServer(private val port: Int) {
    private val server = Server()
    private val handler = ServletHandler()

    init {
        this.server.handler = handler
        val connector = ServerConnector(server)
        connector.port = this.port
        this.server.connectors = arrayOf(connector)
    }

    fun mapControllers(controllers: List<BaseController>) {
        controllers.forEach { controller -> controller.map(this.handler) }
    }

    fun start() {
        this.server.start()
    }

//    TODO: Add logic for stopping the server
}