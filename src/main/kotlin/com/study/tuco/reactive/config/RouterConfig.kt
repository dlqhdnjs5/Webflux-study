package com.study.tuco.reactive.config

import com.study.tuco.reactive.handler.R2dbcRepositoryBookHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration("bookRouterV1")
class RouterConfig {
    @Bean
    fun routeBook(bookHandler: R2dbcRepositoryBookHandler): RouterFunction<ServerResponse> {
        return router {
            listOf(
                GET("/v1/books/{id}", bookHandler::getBook),
                GET("/v1/books", bookHandler::findBooks),
                POST("/v1/books/{function}", bookHandler::requestWebclient),
                POST("/v1/books", bookHandler::createBook),
                PUT("/v1/books/{id}", bookHandler::updateBook),
            )
        }
    }
}