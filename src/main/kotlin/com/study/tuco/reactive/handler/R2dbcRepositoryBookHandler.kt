package com.study.tuco.reactive.handler

import com.study.tuco.reactive.model.Book
import com.study.tuco.reactive.model.BookDto
import com.study.tuco.reactive.model.BookEntity
import com.study.tuco.reactive.model.ErrorResponse
import com.study.tuco.reactive.repository.BookRepositoryR2dbc
import com.study.tuco.reactive.validator.BookValidator
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

@Service
class R2dbcRepositoryBookHandler(
    val bookValidator: BookValidator,
    val bookRepositoryR2dbc: BookRepositoryR2dbc
) {
    fun createBook(
        serverRequest: ServerRequest
    ): Mono<ServerResponse> {
        println("Creating a book")
        return serverRequest.bodyToMono(BookEntity::class.java)
            .doOnNext { it -> validate(it) } // 유효성 검증을 위해 주입받은 BookValidator 를 이용해 doOnNext에서 validate 메서드를 호출
            .flatMap {
                bookRepositoryR2dbc.save(it)
            }
            .flatMap { ServerResponse.ok().build() }
            .onErrorResume(IllegalArgumentException::class.java) { illegalArgumentException ->
                ServerResponse.badRequest().bodyValue(ErrorResponse(HttpStatus.BAD_REQUEST, illegalArgumentException.message!!))
            }
            .onErrorResume(Exception::class.java) { exception ->
                ServerResponse.unprocessableEntity().bodyValue(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.message!!))
            }
        /**
         * Spring webflux에서는 유효성 검증을 진행하는 로직 역시 Operator 체인 안에서 진행됨.
         */
    }

    fun updateBook(serverRequest: ServerRequest): Mono<ServerResponse> {
        val id: Long =  serverRequest.pathVariable("id").toLong()
        println("Updating a book")
        return serverRequest.bodyToMono(BookEntity::class.java)
            .doOnNext { it -> validate(it) }
            .flatMap {
                bookRepositoryR2dbc.save(it.apply { this.bookId = id })
                // Spring Data 패밀리 프로젝트에서는 엔티티 클래스에서 @Id 애너테이션이 추가된 필드의 값이 null 또는 0이 아니면 save() 메서드 호출시 INSERT 가 아닌 UPDATE 쿼리를 실행합니다.
            }
            .flatMap { ServerResponse.ok().build() }
            .onErrorResume(IllegalArgumentException::class.java) { illegalArgumentException ->
                ServerResponse.badRequest().bodyValue(ErrorResponse(HttpStatus.BAD_REQUEST, illegalArgumentException.message!!))
            }
    }

    fun getBook(serverRequest: ServerRequest): Mono<ServerResponse> {
        println("Getting a book")
        val id: Long =  serverRequest.pathVariable("id").toLong()
        return ServerResponse.ok().body(bookRepositoryR2dbc.findById(id), BookEntity::class.java)
            .onErrorResume(Exception::class.java) { exception ->
                ServerResponse.unprocessableEntity().bodyValue(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.message!!))
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

    /**
     * 유효성 검증을 위한 함수
     */
    fun validate(book: BookEntity) {
        val errors = Book::class.simpleName?.let { BeanPropertyBindingResult(book, it) }
        bookValidator.validate(book, errors!!)

        if (errors.hasErrors()){
            throw ServerWebInputException(errors.toString())
        }
    }

    fun findBooks(serverRequest: ServerRequest): Mono<ServerResponse> {
        val bookDto = serverRequest.bodyToMono(BookDto::class.java)
        return bookDto.flatMap { dto ->
            ServerResponse.ok().body(bookRepositoryR2dbc.findAllBy(PageRequest.of(dto.page - 1, dto.size, Sort.by("bookId"))).collectList(), BookEntity::class.java)
        }
    }
}