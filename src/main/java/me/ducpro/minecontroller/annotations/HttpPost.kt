package me.ducpro.minecontroller.annotations

@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class HttpPost(val route: String = "")
