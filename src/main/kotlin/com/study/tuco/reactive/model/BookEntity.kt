package com.study.tuco.reactive.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("book")
data class BookEntity(
    @Id
    var bookId: Long?,
    val titleKorean: String,
    val titleEnglish: String,
    val description: String,
    val author: String,
    val isbn: String,
    val publishDate: LocalDateTime,
    @CreatedDate// auditing 기능 적용
    var createdAt: LocalDateTime?,
    @LastModifiedDate// auditing 기능 적용
    var modifiedAt: LocalDateTime?
)
