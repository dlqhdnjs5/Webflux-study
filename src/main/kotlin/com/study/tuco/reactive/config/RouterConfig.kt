package com.study.tuco.reactive.config

import com.study.tuco.reactive.Handler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration("bookRouterV1")
class RouterConfig {
    @Bean
    fun routeBook(bookHandler: Handler): RouterFunction<ServerResponse> {
        return router {
            listOf(
                GET("/v1/books/{id}", bookHandler::getBook),
                POST("/v1/books", bookHandler::createBook),
                PUT("/v1/books/{id}", bookHandler::updateBook),
            )
        }
    }
}