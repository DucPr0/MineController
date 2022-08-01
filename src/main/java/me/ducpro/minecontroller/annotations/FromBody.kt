package me.ducpro.minecontroller.annotations

/**
 * Can only appear once in a function's parameters.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@MustBeDocumented
annotation class FromBody()
