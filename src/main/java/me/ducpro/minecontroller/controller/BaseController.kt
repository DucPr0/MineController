package me.ducpro.minecontroller.controller

import me.ducpro.minecontroller.annotations.*
import me.ducpro.minecontroller.exceptions.InvalidRouteException
import me.ducpro.minecontroller.mapper.MethodMapper
import me.ducpro.minecontroller.responses.*
import org.eclipse.jetty.servlet.ServletHandler

abstract class BaseController {
    @Suppress("LeakingThis")
    private val methodMapper = MethodMapper(this)

    private val baseRoute: String

    init {
        val annotation =
            this::class.java.getAnnotation(RoutePrefix::class.java)
            ?: throw InvalidRouteException("Controller ${this::class.java.name} has no base route.")
        if (!annotation.route.startsWith('/')) {
            throw InvalidRouteException("Base route of ${this::class.java.name} must start with '/'.")
        }
        if (annotation.route.endsWith('/')) {
            throw InvalidRouteException("Base route of ${this::class.java.name} mustn't end with '/'.")
        }
        this.baseRoute = annotation.route
    }

    fun map(handler: ServletHandler) {
        methodMapper.map(handler, this.baseRoute, this::class.java.declaredMethods)
    }

    protected fun createOkObjectResponse(obj: Any): OkObjectResponse {
        return OkObjectResponse(obj)
    }

    protected fun createOkResponse(): OkResponse {
        return OkResponse()
    }

    protected fun createBadRequestObjectResponse(obj: Any): BadRequestObjectResponse {
        return BadRequestObjectResponse(obj)
    }

    protected fun createBadRequestResponse(): BadRequestResponse {
        return BadRequestResponse()
    }

    protected fun createNotFoundResponse(): NotFoundResponse {
        return NotFoundResponse()
    }

    protected fun createNotFoundObjectResponse(obj: Any): NotFoundObjectResponse {
        return NotFoundObjectResponse(obj)
    }

    protected fun createNoContentResponse(): NoContentResponse {
        return NoContentResponse()
    }
}