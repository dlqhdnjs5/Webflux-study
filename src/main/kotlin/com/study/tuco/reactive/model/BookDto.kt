package com.study.tuco.reactive.model

import org.springframework.data.relational.core.mapping.Table

data class BookDto(
    val page: Int,
    val size: Int
)
