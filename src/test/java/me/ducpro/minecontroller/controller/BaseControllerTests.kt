package me.ducpro.minecontroller.controller

import org.eclipse.jetty.servlet.ServletHandler
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.lang.reflect.Method

class BaseControllerTests {
    @Test
    fun testControllerRouting() {
        val handler = ServletHandler()
        val fauxController = FauxController()
        fauxController.map(handler)
        handler.start()
        println(handler.getMappedServlet("/test/test").servletHolder.servlet)
    }

//    TODO: Implement single-function controllers with a static variable to test if the routes add up.
}