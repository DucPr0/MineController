package me.ducpro.minecontroller.annotations

@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class HttpGet(val route: String = "")
