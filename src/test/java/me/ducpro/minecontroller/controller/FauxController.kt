package me.ducpro.minecontroller.controller

import me.ducpro.minecontroller.annotations.*
import me.ducpro.minecontroller.responses.OkObjectResponse

@RoutePrefix("/test")
class FauxController : BaseController() {
    @HttpGet("test")
    fun testHaha(): OkObjectResponse {
        return this.createOkObjectResponse(5)
    }
}