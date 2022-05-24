package me.ducpro.minecontroller.controller

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.ducpro.minecontroller.annotations.*
import me.ducpro.minecontroller.responses.BaseResponse
import me.ducpro.minecontroller.responses.ObjectResponse
import me.ducpro.minecontroller.responses.OkObjectResponse
import me.ducpro.minecontroller.responses.OkResponse
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import java.lang.IllegalStateException
import java.lang.reflect.Method

abstract class BaseController {
    private val baseRoute: String

    init {
        val annotation =
            this::class.java.getAnnotation(RoutePrefix::class.java)
            ?: throw IllegalStateException("Controller ${this::class.java.name} has no base route.")
        this.baseRoute = annotation.route
    }


    fun map(handler: ServletHandler) {
        val methods = javaClass.declaredMethods
        val routeMappings = HashMap<String, HashMap<Class<out Annotation>, Method>>()

        methods.forEach { method ->
            val requestType = arrayOf(
                HttpGet::class.java,
                HttpPost::class.java,
                HttpPut::class.java,
                HttpDelete::class.java,
            ).find { annotation -> method.isAnnotationPresent(annotation) } ?: return
            val annotation = method.getAnnotation(requestType) ?: return
            val childRoute = this.getRoute(annotation) ?: ""
            val fullRoute = "${this.baseRoute}${if (childRoute.isEmpty()) "" else "/$childRoute"}"

            if (!routeMappings.containsKey(fullRoute)) {
                routeMappings[fullRoute] = HashMap()
            }
            routeMappings[fullRoute]!![requestType] = method
        }

        routeMappings.entries.forEach { (route, requestTypes) ->
            val servlet = object : HttpServlet() {
                override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
                    requestTypes[HttpGet::class.java]?.let { method ->
                        val methodResult = method.invoke(this@BaseController) as BaseResponse
                        handleResponse(resp, methodResult)
                    } ?: kotlin.run {
                        super.doGet(req, resp)
                    }
                }

                override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
                    requestTypes[HttpPost::class.java]?.let { method ->
                        val methodResult = method.invoke(this@BaseController) as BaseResponse
                        handleResponse(resp, methodResult)
                    } ?: kotlin.run {
                        super.doPost(req, resp)
                    }
                }

                override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
                    requestTypes[HttpPut::class.java]?.let { method ->
                        val methodResult = method.invoke(this@BaseController) as BaseResponse
                        handleResponse(resp, methodResult)
                    } ?: kotlin.run {
                        super.doPut(req, resp)
                    }
                }

                override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
                    requestTypes[HttpDelete::class.java]?.let { method ->
                        val methodResult = method.invoke(this@BaseController) as BaseResponse
                        handleResponse(resp, methodResult)
                    } ?: kotlin.run {
                        super.doDelete(req, resp)
                    }
                }
            }
            val servletHolder = ServletHolder(servlet)
            handler.addServletWithMapping(servletHolder, route)
        }
    }

    protected fun createOkObjectResponse(obj: Any): OkObjectResponse {
        return OkObjectResponse(obj)
    }

    protected fun createOkResponse(): OkResponse {
        return OkResponse()
    }

    private fun handleResponse(resp: HttpServletResponse, result: BaseResponse) {
        resp.status = result.statusCode

        if (result is ObjectResponse) {
            val jsonResponse = result.getJsonResponseObject()
            resp.contentType = "application/json"
            resp.outputStream.write(jsonResponse.toByteArray())
            resp.outputStream.flush()
        }
    }

    private fun getRoute(annotation: Annotation) : String? {
        if (annotation is HttpGet) {
            return annotation.route
        }
        if (annotation is HttpPost) {
            return annotation.route
        }
        if (annotation is HttpPut) {
            return annotation.route
        }
        if (annotation is HttpDelete) {
            return annotation.route
        }

        return null
    }
}