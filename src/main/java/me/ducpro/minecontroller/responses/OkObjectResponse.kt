package me.ducpro.minecontroller.responses

import jakarta.servlet.http.HttpServletResponse

class OkObjectResponse(responseObject: Any)
    : ObjectResponse(HttpServletResponse.SC_OK, responseObject) {
}