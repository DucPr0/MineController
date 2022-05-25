package me.ducpro.minecontroller.responses

import jakarta.servlet.http.HttpServletResponse

class NotFoundResponse : StatusCodeResponse(HttpServletResponse.SC_NOT_FOUND) {
}