package com.study.tuco.reactive.config

import com.study.tuco.reactive.model.BookEntity
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.toEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class WebClientExample {
    companion object {
        val HOST_URL = "http://localhost:8084"
    }

    val log = LoggerFactory.getLogger(WebClientExample::class.java)

    fun exampleWebClientPost() {
        val webclient: WebClient = WebClient.create()
        val requestBody = BookEntity(
            bookId = null,
            titleKorean = "Korean Title",
            titleEnglish = "English Title",
            description = "Description",
            author = "Author",
            isbn = "ISBN",
            publishDate = LocalDateTime.now(),
            createdAt = null,
            modifiedAt = null
        )


        val response: Mono<ResponseEntity<Void>> = webclient
            .post().uri("${HOST_URL}/v1/books")
            .bodyValue(requestBody)
            .retrieve() // response를 어떤 형태로 얻을지에 대한 프로세스의 '시작' 을 선언하는 역할
            .toEntity(Void::class.java) // 파라미터로 주어진 클래스의 형태로 변환한 response body가 포함된 ResponseEntity를 반환

        response.subscribe { entity ->
            log.info("Response status: ${entity.statusCode}")
            log.info("Response header: ${entity.headers}")
        }
    }

    fun exampleWebClientPut() {
        val webclient: WebClient = WebClient.create(HOST_URL)
        val requestBody = BookEntity(
            bookId = 1,
            titleKorean = "updated Korean Title",
            titleEnglish = "updated English Title",
            description = "updated Description",
            author = "updated Author",
            isbn = "updated ISBN",
            publishDate = LocalDateTime.now(),
            createdAt = null,
            modifiedAt = null
        )


        val response: Mono<ResponseEntity<Void>> = webclient
            .put().uri("/v1/books/{book-id}", 1)
            .bodyValue(requestBody)
            .retrieve()
            .toEntity(Void::class.java)

        response.subscribe { entity ->
            log.info("Response status: ${entity.statusCode}")
            log.info("Response header: ${entity.headers}")
        }
    }

    fun exampleWebClientGet() {
        val webclient: WebClient = WebClient.create(HOST_URL)
        val response: Flux<BookEntity> = webclient
            .get().uri { uriBuilder ->
                uriBuilder.path("/v1/books")
                    .queryParam("page", 1)
                    .queryParam("size", 5)
                    .build() }
            .retrieve()
            .bodyToFlux(BookEntity::class.java)
        // bodyToFlux 를 통해 response body를 파라미터로 전달된 타입의 객체로 디코딩 합니다.
        // bodyToFlux는 bodyToMono와 달리 Java Collection 타입의 response body를 수신합니다.

        response.subscribe { entity ->
            log.info("Response: $entity")
        }
    }
}