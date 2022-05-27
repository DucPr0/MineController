package me.ducpro.minecontroller.responses

import jakarta.servlet.http.HttpServletResponse

class NoContentResponse : BaseResponse(HttpServletResponse.SC_NO_CONTENT) {
}