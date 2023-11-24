package com.newspeed.user.global.exception.model

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class ApplicationException(
    private val httpStatus: HttpStatus,
    override val message: String
): RuntimeException(message) {

    fun toResponseEntity(): ResponseEntity<ExceptionResponse> = ResponseEntity(
        message.toExceptionResponse(),
        httpStatus
    )
}

fun String.toExceptionResponse(): ExceptionResponse = ExceptionResponse(
    errorMessage = this
)