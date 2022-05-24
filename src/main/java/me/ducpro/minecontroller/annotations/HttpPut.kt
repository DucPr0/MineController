package me.ducpro.minecontroller.annotations

@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class HttpPut(val route: String = "")
