package com.unitx.hyphen_kotlin

sealed interface ResultUnit<out D, out E: ErrorBlueprint> {
    data class SuccessUnit<out D, out E: ErrorBlueprint>(val data : D): ResultUnit<D, E>
    data class ErrorUnit<out D, out E: ErrorBlueprint>(val error: E): ResultUnit<D, E>
}