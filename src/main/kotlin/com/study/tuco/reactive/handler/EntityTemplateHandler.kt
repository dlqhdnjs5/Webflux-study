package com.study.tuco.reactive.handler

import com.study.tuco.reactive.model.BookEntity
import org.springframework.data.domain.PageRequest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

@Service
class EntityTemplateHandler(
    val r2dbcEntityTemplate: R2dbcEntityTemplate
) {
    fun createBook(bookEntity: BookEntity) {
        verifyExitIsbn(bookEntity.isbn)
            .doOnNext { r2dbcEntityTemplate.insert(bookEntity) }
    }

    fun updateBook(bookEntity: BookEntity) {
        findVerifiedBook(bookEntity.isbn)
            .doOnNext { r2dbcEntityTemplate.update(bookEntity) }
    }

    fun findBooks(page: Int, size: Int): Mono<List<BookEntity>> {
        return r2dbcEntityTemplate.select(BookEntity::class.java)
            .matching(Query.empty().with(PageRequest.of(page, size)))
            .all()
            .collectList()

        /*matching 메서드는 R2dbcEntityTemplate에서 쿼리 조건을 설정하는 데 사용됩니다. 이 메서드는 Query 객체를 받아서 데이터베이스 쿼리를 구성합니다. Query 객체는 필터링 조건, 정렬, 페이징 등을 포함할 수 있습니다.*/
    }

    fun findBooks(page: Long, size: Long): Mono<List<BookEntity>> {
        return r2dbcEntityTemplate
            .select(BookEntity::class.java)
            .count()
            .flatMap { total ->
                val skipAndTake = getSkipAndTake(total, page, size)
                r2dbcEntityTemplate
                    .select(BookEntity::class.java)
                    .all()
                    .sort()
                    .skip(skipAndTake.t1)
                    .take(skipAndTake.t2)
                    .collectList()
            }
    }

    private fun getSkipAndTake(total: Long, movePage: Long, size: Long): Tuple2<Long, Long> {
        val totalPages = Math.ceil(total.toDouble() / size).toLong()
        val page = if (movePage > totalPages) totalPages else movePage
        val skip = if (total - (page * size) < 0) 0 else total - (page * size)
        val take = if (total - (page * size) < 0) total - ((page - 1) * size) else size

        return Tuples.of(skip, take)
    }

    private fun verifyExitIsbn(isbn: String): Mono<Unit> {
        return r2dbcEntityTemplate.selectOne(Query.query(where("isbn").`is`(isbn)), BookEntity::class.java)
            .flatMap { isbn ->
                if (isbn != null) {
                    Mono.error(RuntimeException("ISBN already exists"))
                } else {
                    Mono.empty()
                }
            }
    }

    private fun findVerifiedBook(isbn: String): Mono<BookEntity> {
        return r2dbcEntityTemplate.selectOne(Query.query(where("isbn").`is`(isbn)), BookEntity::class.java)
            .switchIfEmpty(Mono.error(RuntimeException("ISBN NOT exists")))
    }
}