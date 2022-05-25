package me.ducpro.minecontroller.controller

import me.ducpro.minecontroller.annotations.*
import me.ducpro.minecontroller.responses.OkObjectResponse
import me.ducpro.minecontroller.responses.OkResponse
import org.bukkit.Bukkit

@RoutePrefix("/test")
class TestController : BaseController() {
    @HttpGet("test/{id}/a/{id}")
    fun test(@FromPath("id") a: Int, @FromPath("id") b: Double): OkObjectResponse {
        println(a)
        println(b)
        return this.createOkObjectResponse(5)
    }

    @HttpGet("test/{id}/a")
    fun test2(@FromPath("id") b: Int, @FromQuery("query") a: Int): OkObjectResponse {
        return this.createOkObjectResponse(a + b)
    }

    @HttpGet("test")
    fun test4(): OkObjectResponse {
        return this.createOkObjectResponse("haha")
    }

    @HttpPost("post")
    fun test3(@FromBody a: String): OkResponse {
        Bukkit.broadcastMessage(a)
//        println(a)
        return this.createOkResponse()
    }
}