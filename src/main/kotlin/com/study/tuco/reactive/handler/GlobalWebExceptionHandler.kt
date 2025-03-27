package com.study.tuco.reactive.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.study.tuco.reactive.model.ErrorResponse
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Order(-2) // DefaultErrorWebExceptionHandler bean의 우선순위 (-1) 보다 높은 우선순위인 -2를 지정합니다.
@Configuration
class GlobalWebExceptionHandler(
    private val objectMapper: ObjectMapper
): ErrorWebExceptionHandler {
    val log = org.slf4j.LoggerFactory.getLogger(this::class.java)

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        return handleException(exchange, ex)
    }

    fun handleException(serverWebExchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val bufferFactory: DataBufferFactory = serverWebExchange.response.bufferFactory()
        serverWebExchange.response.headers.contentType = MediaType.APPLICATION_JSON

        log.error("Error: ", ex)

        val (status, message) = when (ex) {
            is IllegalArgumentException -> HttpStatus.BAD_REQUEST to ex.message
            is ResponseStatusException -> ex.status to ex.reason
            else -> HttpStatus.INTERNAL_SERVER_ERROR to ex.message
        }

        val errorResponse = ErrorResponse(status, message)
        serverWebExchange.response.statusCode = status

        val dataBuffer = try {
            bufferFactory.wrap(objectMapper.writeValueAsBytes(errorResponse))
        } catch (e: Exception) {
            bufferFactory.wrap(ByteArray(0))
        }

        return serverWebExchange.response.writeWith(Mono.just(dataBuffer))
    }
}