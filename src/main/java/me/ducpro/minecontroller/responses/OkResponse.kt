package me.ducpro.minecontroller.responses

import jakarta.servlet.http.HttpServletResponse

class OkResponse : StatusCodeResponse(HttpServletResponse.SC_OK)