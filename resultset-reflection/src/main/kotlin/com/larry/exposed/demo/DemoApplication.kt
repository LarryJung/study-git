package com.larry.exposed.demo

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.id.IntIdTable
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import javax.sql.DataSource

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

val config = HikariConfig().apply {
    jdbcUrl = "jdbc:mysql://localhost:3306/exposed_db?useSSL=false&serverTimezone=Asia/Seoul&autoReconnect=true"
    driverClassName = "com.mysql.cj.jdbc.Driver"
    username = "root"
    password = ""
}
val dataSource: DataSource = HikariDataSource(config)


object Cities: IntIdTable() {
    val name = varchar("name", 50)
}