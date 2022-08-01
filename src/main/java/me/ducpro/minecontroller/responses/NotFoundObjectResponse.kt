package me.ducpro.minecontroller.responses

import jakarta.servlet.http.HttpServletResponse

class NotFoundObjectResponse(responseObject: Any) :
    ObjectResponse(HttpServletResponse.SC_NOT_FOUND, responseObject)