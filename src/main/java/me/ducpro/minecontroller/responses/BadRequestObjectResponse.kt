package me.ducpro.minecontroller.responses

import jakarta.servlet.http.HttpServletResponse

class BadRequestObjectResponse(responseObject: Any) :
    ObjectResponse(HttpServletResponse.SC_BAD_REQUEST, responseObject) {
}