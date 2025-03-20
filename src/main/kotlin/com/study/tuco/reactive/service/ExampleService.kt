package com.study.tuco.reactive.service

import com.study.tuco.reactive.model.Book
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ExampleService {
    fun getBook(bookId: String): Mono<Book> {
        return Mono.just(Book("1", "The Stand", "Stephen King"))
    }
}