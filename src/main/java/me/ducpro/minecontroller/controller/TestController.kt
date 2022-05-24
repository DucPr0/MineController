package me.ducpro.minecontroller.controller

import me.ducpro.minecontroller.annotations.HttpGet
import me.ducpro.minecontroller.annotations.HttpPost
import me.ducpro.minecontroller.annotations.RoutePrefix
import me.ducpro.minecontroller.responses.OkObjectResponse
import me.ducpro.minecontroller.responses.OkResponse
import org.bukkit.Bukkit

@RoutePrefix("/test")
class TestController : BaseController() {
    @HttpGet("test")
    fun test(): OkObjectResponse {
        return this.createOkObjectResponse(listOf(4, 5, 6))
    }

    @HttpGet("")
    fun test2(): OkObjectResponse {
        return this.createOkObjectResponse(4)
    }

    @HttpPost("test")
    fun test3(): OkResponse {
        Bukkit.broadcastMessage("testing!")
        return this.createOkResponse()
    }
}