package com.study.tuco.reactive.validator

import com.study.tuco.reactive.model.Book
import com.study.tuco.reactive.model.BookEntity
import org.springframework.stereotype.Component
import org.springframework.validation.Validator

@Component("bookValidator")
class BookValidator: Validator {
    override fun supports(clazz: Class<*>): Boolean {
        return BookEntity::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: org.springframework.validation.Errors) {
        val book = target as BookEntity
        if (book.titleKorean.isBlank()) {
            errors.rejectValue("title", "title.empty")
        }
    }
}