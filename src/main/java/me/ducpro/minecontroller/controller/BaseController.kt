package me.ducpro.minecontroller.controller

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.ducpro.minecontroller.annotations.*
import me.ducpro.minecontroller.responses.*
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import java.lang.ClassCastException
import java.lang.IllegalStateException
import java.lang.StringBuilder
import java.lang.reflect.Method
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern
import kotlin.collections.HashMap

abstract class BaseController {
    companion object {
        val gson: Gson = GsonBuilder().create()
    }

    private val baseRoute: String

    init {
        val annotation =
            this::class.java.getAnnotation(RoutePrefix::class.java)
            ?: throw IllegalStateException("Controller ${this::class.java.name} has no base route.")
        this.baseRoute = annotation.route
    }


    fun map(handler: ServletHandler) {
        val methods = this::class.java.declaredMethods
        val exactRouteMappings = HashMap<String, HashMap<Class<out Annotation>, Method>>()
        val paramRouteMappings = HashMap<String, HashMap<String, HashMap<Class<out Annotation>, Method>>>()

        methods.forEach { method ->
            val requestType = arrayOf(
                HttpGet::class.java,
                HttpPost::class.java,
                HttpPut::class.java,
                HttpDelete::class.java,
            ).find { annotation -> method.isAnnotationPresent(annotation) } ?: kotlin.run {
                Logger.getLogger(this::class.java.name).log(Level.WARNING,
                    "Unable to associate HttpRequestType with method ${method.name} in ${this::class.java.name}.")
                return@forEach
            }
            val annotation = method.getAnnotation(requestType)
            val childRoute = this.getRoute(annotation)
            if (childRoute.contains('*')) {
                throw IllegalStateException("Annotation route for method ${method.name} must not contain wildcard *.")
            }
            childRoute.split('/').find { route ->
                ((route.startsWith('{')) && !route.endsWith('}'))
                        || (!route.startsWith('{') && route.endsWith('}'))
            }?.let { invalidRoute ->
                throw IllegalStateException("Annotation route for method ${method.name} contains invalid path $invalidRoute")
            }

            val split = this.splitRoute(childRoute)

            if (split.second.isEmpty()) {
                val fullRoute = split.first
                if (!exactRouteMappings.containsKey(fullRoute)) {
                    exactRouteMappings[fullRoute] = HashMap()
                }
                exactRouteMappings[fullRoute]!![requestType] = method
            } else {
                val prefix = split.first
                val regexSuffix = this.getRegexRoute(split.second)
                if (!paramRouteMappings.containsKey(prefix)) {
                    paramRouteMappings[prefix] = HashMap()
                }
                if (!paramRouteMappings[prefix]!!.containsKey(regexSuffix)) {
                    paramRouteMappings[prefix]!![regexSuffix] = HashMap()
                }
                paramRouteMappings[prefix]!![regexSuffix]!![requestType] = method
            }

//            Verify that all parameters contain data sources.
            method.parameters.forEach { parameter ->
                arrayOf(
                    FromBody::class.java,
                    FromPath::class.java,
                    FromQuery::class.java
                ).find { annotation -> parameter.isAnnotationPresent(annotation) }
                    ?: throw IllegalStateException("Method ${method.name} has parameters with no data source.")
            }

//            Verify for all @FromPath parameters that the route contains the defined names.
            method.parameters.forEach { parameter ->
                if (!parameter.isAnnotationPresent(FromPath::class.java)) return@forEach
                val parameterPathname = parameter.getAnnotation(FromPath::class.java).name

                val methodRoute = "${this.baseRoute}${if (childRoute.isEmpty()) "" else "/$childRoute"}"
                val split = methodRoute.split('/')
                split.withIndex().find { (_, path) ->
                    if (!path.startsWith('{')) false
                    else path.substring(1, path.length - 1) == parameterPathname
                } ?: throw IllegalStateException("Cannot find placeholder {${parameterPathname}} on path ${methodRoute}.")
            }
        }

//        Map routes
        exactRouteMappings.entries.forEach { (route, requestTypes) ->
            val servlet = object : HttpServlet() {
                override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
                    requestTypes[HttpGet::class.java]?.let { method ->
                        val actionResult = invokeAction(req, method)
                        handleResponse(resp, actionResult)
                    } ?: kotlin.run {
                        super.doGet(req, resp)
                    }
                }

                override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
                    requestTypes[HttpPost::class.java]?.let { method ->
                        val actionResult = invokeAction(req, method)
                        handleResponse(resp, actionResult)
                    } ?: kotlin.run {
                        super.doPost(req, resp)
                    }
                }

                override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
                    requestTypes[HttpPut::class.java]?.let { method ->
                        val actionResult = invokeAction(req, method)
                        handleResponse(resp, actionResult)
                    } ?: kotlin.run {
                        super.doPut(req, resp)
                    }
                }

                override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
                    requestTypes[HttpDelete::class.java]?.let { method ->
                        val actionResult = invokeAction(req, method)
                        handleResponse(resp, actionResult)
                    } ?: kotlin.run {
                        super.doDelete(req, resp)
                    }
                }
            }
            val servletHolder = ServletHolder(servlet)
            handler.addServletWithMapping(servletHolder, route)
        }

        paramRouteMappings.entries.forEach { (route, paths) ->
            val servlet = object : HttpServlet() {
                override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
                    val matchingController = paths.keys.find { regex -> matchesPathInfo(regex, req.pathInfo) }
                        ?: return handleResponse(resp, createNoMatchingControllerResponse(req.requestURI))

                    paths[matchingController]!![HttpGet::class.java]?.let { method ->
                        val actionResult = invokeAction(req, method)
                        handleResponse(resp, actionResult)
                    } ?: kotlin.run {
                        super.doGet(req, resp)
                    }
                }

                override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
                    val matchingController = paths.keys.find { regex -> matchesPathInfo(regex, req.pathInfo) }
                        ?: return handleResponse(resp, createNoMatchingControllerResponse(req.requestURI))

                    paths[matchingController]!![HttpPost::class.java]?.let { method ->
                        val actionResult = invokeAction(req, method)
                        handleResponse(resp, actionResult)
                    } ?: kotlin.run {
                        super.doPost(req, resp)
                    }
                }

                override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
                    val matchingController = paths.keys.find { regex -> matchesPathInfo(regex, req.pathInfo) }
                        ?: return handleResponse(resp, createNoMatchingControllerResponse(req.requestURI))

                    paths[matchingController]!![HttpPut::class.java]?.let { method ->
                        val actionResult = invokeAction(req, method)
                        handleResponse(resp, actionResult)
                    } ?: kotlin.run {
                        super.doPut(req, resp)
                    }
                }

                override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
                    val matchingController = paths.keys.find { regex -> matchesPathInfo(regex, req.pathInfo) }
                        ?: return handleResponse(resp, createNoMatchingControllerResponse(req.requestURI))

                    paths[matchingController]!![HttpDelete::class.java]?.let { method ->
                        val actionResult = invokeAction(req, method)
                        handleResponse(resp, actionResult)
                    } ?: kotlin.run {
                        super.doDelete(req, resp)
                    }
                }
            }
            val servletHolder = ServletHolder(servlet)
            handler.addServletWithMapping(servletHolder, "$route/*")
        }
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

    private fun invokeAction(req: HttpServletRequest, method: Method) : BaseResponse {
        val result: Any?
        val invokeParameters = mutableListOf<Any>()

        val requestType = arrayOf(
            HttpGet::class.java,
            HttpPost::class.java,
            HttpPut::class.java,
            HttpDelete::class.java,
        ).find { annotation -> method.isAnnotationPresent(annotation) }!!

        val methodAnnotation = method.getAnnotation(requestType)
        val childRoute = this.getRoute(methodAnnotation)
        val methodRoute = "${this.baseRoute}${if (childRoute.isEmpty()) "" else "/$childRoute"}"

        method.parameters.forEach { parameter ->
            val dataSource = arrayOf(
                FromBody::class.java,
                FromPath::class.java,
                FromQuery::class.java
            ).find { annotation -> parameter.isAnnotationPresent(annotation) }!!
            when (val annotation = parameter.getAnnotation(dataSource)) {
                is FromBody -> {
                    val requestBody = req.reader
                    val fromBodyType = parameter.type

                    val bodyParameter = try {
                        gson.fromJson(requestBody, fromBodyType)
                    } catch (exception: JsonSyntaxException) {
                        return this.createBadRequestObjectResponse("Unable to parse request body.")
                    }

                    invokeParameters.add(bodyParameter)
                }
                is FromPath -> {
                    val queriedRoute = req.requestURI

                    val split = methodRoute.split('/')
                    val querySplit = queriedRoute.split('/')

                    val data = split.zip(querySplit).find { (path, _) ->
                        if (!path.startsWith('{')) false
                        else path.substring(1, path.length - 1) == annotation.name
                    }!!.second

                    val dataType = parameter.type
                    val pathParameter = try {
                        gson.fromJson(data, dataType)
                    } catch (exception: JsonSyntaxException) {
                        return this.createBadRequestObjectResponse("Unable to parse $data to type $dataType")
                    }

                    invokeParameters.add(pathParameter)
                }
                is FromQuery -> {
                    val data = req.getParameter(annotation.name)
                        ?: return this.createBadRequestObjectResponse("URI parameter ${annotation.name} expected, not found.")
                    val dataType = parameter.type
                    val queryParameter = try {
//                        An easy way to convert from string to other primitive data types, but not the best.
                        gson.fromJson(data, dataType)
                    } catch (exception: JsonSyntaxException) {
                        return this.createBadRequestObjectResponse("Unable to parse $data to type $dataType")
                    }

                    invokeParameters.add(queryParameter)
                }
            }
        }

        result = InvokeMethodHelper.invokeMethod(method, this, invokeParameters.toTypedArray())
        if (result is BaseResponse) {
            return result
        } else {
            throw ClassCastException("Unsuccessfully attempted to cast ${method.name}'s return value to BaseResponse.")
        }
    }

    private fun handleResponse(resp: HttpServletResponse, result: BaseResponse) {
        resp.status = result.statusCode

        if (result is ObjectResponse) {
            val jsonResponse = result.getJsonResponseObject()
            resp.contentType = "application/json"
            resp.setContentLength(jsonResponse.toByteArray().size)
            resp.outputStream.write(jsonResponse.toByteArray())
            resp.outputStream.flush()
        }
    }

    private fun getRoute(annotation: Annotation): String {
        return when (annotation) {
            is HttpGet -> annotation.route
            is HttpPost -> annotation.route
            is HttpPut -> annotation.route
            is HttpDelete -> annotation.route
            else -> throw IllegalStateException("Expected annotation $annotation to be HTTP request, found otherwise.")
        }
    }

    /**
     * Usage: splitRoute("path1/path2/{id}/path3") would return ("base/path1/path2", "{id}/path3")
     */
    private fun splitRoute(childRoute: String): Pair<String, String> {
        val split = childRoute.split('/')
        val builder = StringBuilder()

        val prefix = split.takeWhile { path -> !path.startsWith('{') }
        val suffix = split.takeLast(split.size - prefix.size)

        builder.append(this.baseRoute)
        prefix.forEach { str -> builder.append("/$str") }
        val childPrefix = builder.toString()

        builder.clear()
        suffix.forEach { str -> builder.append("/$str") }
        val childSuffix = if (suffix.isEmpty()) "" else builder.toString().substring(1)

        return Pair(childPrefix, childSuffix)
    }

    /**
     * Usage: getRegexRoute("a/{id}/b/c/{id}") would return "a/[^/]+/b/c/[^/]+"
     */
    private fun getRegexRoute(route: String): String {
        val regexRoute = route.split('/').map { str ->
            if (str.startsWith('{')) "[^/]+"
            else str
        }
        val builder = StringBuilder("^")
        regexRoute.forEach { str -> builder.append("/$str") }
        builder.append('$')
        return builder.toString()
    }

    /**
     * A wrapper for Pattern.matches that performs a null-check on the matched string.
     */
    private fun matchesPathInfo(regex: String, pathInfo: String?): Boolean {
        return pathInfo?.let { _ -> Pattern.matches(regex, pathInfo) } ?: false
    }

    private fun createNoMatchingControllerResponse(uri: String): NotFoundObjectResponse {
        return this.createNotFoundObjectResponse("No HTTP resource was found that matches the request URI $uri")
    }
}