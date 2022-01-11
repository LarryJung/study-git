package com.example.kotlinquerydsl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

/**
 * @author Larry
 */
@Entity
class Post(
    @Id
    @GeneratedValue
    var id: Long? = null,

    val title: String,
    val body: String,
    val writerId: Long
)

interface PostRepository : JpaRepository<Post, Long>, PostQueryDslRepository

interface PostQueryDslRepository {
    fun doQuery_java_style(param: Param): List<Post>
    fun doQuery_kotlin_style(param: Param): List<Post>
    fun doQuery_kotlin_my_style(param: Param): List<Post>
}

class PostQueryDslRepositoryImpl
    : QuerydslRepositorySupport(Post::class.java), PostQueryDslRepository {
    override fun doQuery_java_style(param: Param): List<Post> {
        val qPost = QPost.post

        val query = from(qPost)
        if (param.id != null) {
            query.where(qPost.id.eq(param.id))
        }
        if (param.title.isNullOrBlank()) {
            query.where(qPost.title.eq(param.title))
        }
        if (param.body.isNullOrBlank()) {
            query.where(qPost.body.eq(param.body))
        }
        if (param.writerId != null) {
            query.where(qPost.writerId.eq(param.writerId))
        }
        return query.fetch() ?: emptyList()
    }

    override fun doQuery_kotlin_style(param: Param): List<Post> {
        val qPost = QPost.post

        val query = from(qPost)
        param.id.let { query.where(qPost.id.eq(it)) }
        param.title.let { query.where(qPost.title.eq(it)) }
        param.body.let { query.where(qPost.body.eq(it)) }
        param.writerId.let { query.where(qPost.writerId.eq(it)) }
        return query.fetch() ?: emptyList()
    }

    override fun doQuery_kotlin_my_style(param: Param): List<Post> {
        val qPost = QPost.post

        return from(qPost)
            .whereNotEmpty(param.id) { qPost.id.eq(it) }
            .whereNotEmpty(param.title) { qPost.title.eq(it) }
            .whereNotEmpty(param.body) { qPost.body.eq(it) }
            .whereNotEmpty(param.writerId) { qPost.writerId.eq(it) }
            .fetchList()
    }
}

data class Param(
    val id: Long?,
    val title: String?,
    val body: String?,
    val writerId: Long?
)


@SuppressWarnings("ControlFlowWithEmptyBody")
fun complicated(): Unit =
    if (a) {
    } else if (b) {
        if (c) {
        } else {
        }
    } else if (d) {
        if (e) {
            println("hello")
        } else {
        }
    } else {
    }