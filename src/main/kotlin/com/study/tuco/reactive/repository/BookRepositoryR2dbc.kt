package com.study.tuco.reactive.repository

import com.study.tuco.reactive.model.BookEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface BookRepositoryR2dbc: ReactiveCrudRepository<BookEntity, Long> {
    fun findByIsbn(isbn: String): Mono<BookEntity>
}