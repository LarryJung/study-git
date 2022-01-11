package com.example.kotlinquerydsl

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class KotlinQuerydslApplicationTests {

    @Autowired
    lateinit var postRepository: PostRepository

    @Test
    fun contextLoads() {
        postRepository.save(Post(title = "title", body = "body", writerId = 1L))

        val param = Param(1L, "title", "body", 1L)

        postRepository.doQuery_1(param)
        postRepository.doQuery_2(param)
    }

}
