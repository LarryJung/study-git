package com.example.routingdatasource

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val testService: TestService
) {

    @GetMapping("/query")
    fun query(): Any {
        return testService.findAll()
    }

    @PostMapping("/city/{name}")
    fun create(@PathVariable name: String): Any {
        return testService.createCity(name)
    }

}

@Service
class TestService {

    @Transactional(readOnly = false)
    fun createCity(name: String): Unit {
        Cities.insert {
            it[this.name] = name
        } get Cities.id
    }

    @Transactional(readOnly = true)
    fun findAll() = Cities.selectAll().map { it.toString() }

}