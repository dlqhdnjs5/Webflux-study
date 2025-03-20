package com.study.tuco.reactive.config

import com.study.tuco.reactive.filter.CustomHandlerFilterFunction
import com.study.tuco.reactive.model.Book
import com.study.tuco.reactive.service.ExampleService
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Configuration
class ExampleRouterConfig(
    val exampleService: ExampleService
) {
    val log = org.slf4j.LoggerFactory.getLogger(this::class.java)

    val getBookRouter: RouterFunction<ServerResponse> = RouterFunctions.route(
        RequestPredicates.GET("/v1/router/books/{book-id}"), HandlerFunction { request -> log.info("request : $request")
            exampleService.getBook(request.pathVariable("book-id")).flatMap {
                ServerResponse.ok().body(Mono.just(it), Book::class.java)
            }
        }
    ).filter(CustomHandlerFilterFunction()) // 필터 적용
}