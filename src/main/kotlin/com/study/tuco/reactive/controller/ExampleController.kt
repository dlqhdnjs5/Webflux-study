package com.study.tuco.reactive.controller

import com.study.tuco.reactive.service.ExampleService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono


@RequestMapping(path = arrayOf("/v1/books"))
@RestController
class ExampleController(
    private val exampleService: ExampleService
) {
    val logger = LoggerFactory.getLogger(ExampleController::class.java)

    @PatchMapping("/{book-id}")
    fun getBook(@PathVariable("book-id") bookId: String): Mono<ServerResponse> = exampleService.getBook(bookId).flatMap { book ->
        ServerResponse.ok().bodyValue(book)
    }
}