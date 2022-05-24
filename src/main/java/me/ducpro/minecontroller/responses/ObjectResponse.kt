package me.ducpro.minecontroller.responses

import com.google.gson.Gson
import com.google.gson.GsonBuilder

open class ObjectResponse(
    statusCode: Int,
    private val responseObject: Any
) : BaseResponse(statusCode) {
    companion object {
        val gson: Gson = GsonBuilder().create()
    }

    fun getJsonResponseObject(): String {
        return gson.toJson(this.responseObject)
    }
}