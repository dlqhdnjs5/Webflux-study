package com.study.tuco.reactive.model

import org.springframework.http.HttpStatus

data class ErrorResponse(val HttpStatus: HttpStatus, val message: String?)
