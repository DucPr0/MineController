package me.ducpro.minecontroller.controller

import me.ducpro.minecontroller.annotations.FromBody
import me.ducpro.minecontroller.annotations.HttpGet
import me.ducpro.minecontroller.annotations.HttpPost
import me.ducpro.minecontroller.annotations.RoutePrefix
import me.ducpro.minecontroller.responses.OkObjectResponse
import me.ducpro.minecontroller.responses.OkResponse
import org.bukkit.Bukkit

@RoutePrefix("/test")
class TestController : BaseController() {
    @HttpGet("test")
    fun test(@FromBody a: List<Int>): OkObjectResponse {
        return this.createOkObjectResponse(5)
    }

    @HttpGet("")
    fun test2(): OkObjectResponse {
        return this.createOkObjectResponse(4)
    }

    @HttpPost("test")
    fun test3(@FromBody a: String): OkResponse {
        Bukkit.broadcastMessage(a)
//        println(a)
        return this.createOkResponse()
    }
}