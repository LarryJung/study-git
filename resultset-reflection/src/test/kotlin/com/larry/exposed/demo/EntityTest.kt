package com.larry.exposed.demo

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.sql.ResultSet
import java.util.*

/**
 * @author Larry
 */
data class CityDto(val id: Int, val name: String)

class EntityTest {
    @Test
    fun test1() {
        Database.connect(dataSource)

        transaction {
            // print sql to std-out
            addLogger(
                StdOutSqlLogger,
//                Slf4jSqlDebugLogger
            )
            SchemaUtils.create(Cities)
            // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
            val stPeteId = Cities.insert {
                it[name] = "St. Petersburg"
            } get Cities.id

            val cityDtoList_with_no_refied = exec("SELECT * FROM Cities") { rs -> recursiveExtract(rs, CityDto::class.java) } ?: listOf()
            val cityDtoList_withrefied: List<CityDto> = exec("SELECT * FROM Cities") { rs -> rs.toList() } ?: listOf()

            println(cityDtoList_with_no_refied)
            println(cityDtoList_withrefied)
        }
    }
}

fun <T> recursiveExtract(resultSet: ResultSet, clazz: Class<T>): List<T> {
    fun recursiveExtract_(resultSet: ResultSet, list: LinkedList<T>): List<T> {
        return if (resultSet.next()) {
            list.add(resultSet.mapTo(clazz))
            recursiveExtract_(resultSet, list)
        } else {
            list
        }
    }
    return recursiveExtract_(resultSet, LinkedList())
}

fun <T> ResultSet.mapTo(clazz: Class<T>): T {
    val constructor = clazz.getDeclaredConstructor(*clazz.declaredFields.map { it.type }.toTypedArray())
    val dataList = clazz.declaredFields.map {
        val nameField = clazz.getDeclaredField(it.name)
        nameField.isAccessible = true
        this.getObject(it.name, nameField.type)
    }
    return constructor.newInstance(*dataList.toTypedArray())
}

inline fun <reified T> ResultSet.toList() = recursiveExtract(this, T::class.java)