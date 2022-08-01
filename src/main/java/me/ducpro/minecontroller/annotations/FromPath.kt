package me.ducpro.minecontroller.annotations

@Target(AnnotationTarget.VALUE_PARAMETER)
@MustBeDocumented
annotation class FromPath(val name: String)
