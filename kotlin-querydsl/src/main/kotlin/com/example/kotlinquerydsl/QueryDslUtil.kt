package com.example.kotlinquerydsl

import com.querydsl.core.Fetchable
import com.querydsl.core.types.Predicate
import com.querydsl.jpa.JPQLQuery

/**
 * @author Larry
 */
inline fun <T> T?.notEmpty(block: (T) -> Unit) {
    if (this == null || (this is String && this.trim() === "")) return

    block(this)
}

fun <C, T> JPQLQuery<C>.whereNotEmpty(
    param: T?,
    predicate: (T) -> Predicate?,
): JPQLQuery<C> {
    param.notEmpty {
        this.where(predicate(it))
    }
    return this
}

fun <T> Fetchable<T>.fetchList(): List<T> = this.fetch() ?: emptyList()