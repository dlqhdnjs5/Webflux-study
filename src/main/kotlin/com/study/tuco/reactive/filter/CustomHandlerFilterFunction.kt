package com.study.tuco.reactive.filter

import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class CustomHandlerFilterFunction: HandlerFilterFunction<ServerResponse, ServerResponse> {
    override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
       val path = request.requestPath().value()

        return next.handle(request).doAfterTerminate {
            println("Request path: $path , status : ${request.exchange().response.statusCode}")
        }
    }
}