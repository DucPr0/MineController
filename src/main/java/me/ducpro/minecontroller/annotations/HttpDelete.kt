package me.ducpro.minecontroller.annotations

@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class HttpDelete(val route: String = "")
