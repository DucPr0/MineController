package me.ducpro.minecontroller.responses

import jakarta.servlet.http.HttpServletResponse

class BadRequestResponse : StatusCodeResponse(HttpServletResponse.SC_BAD_REQUEST)