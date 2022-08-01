package me.ducpro.minecontroller.annotations

@Target(AnnotationTarget.VALUE_PARAMETER)
@MustBeDocumented
annotation class FromQuery(val name: String)
