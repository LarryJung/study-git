package com.larry.exposed.demo.demo.com.larry.exposed.demo

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

/**
 * @author Larry
 */

//class Book(id: EntityID<Long>) : LongEntity(id) {
//    companion object : LongEntityClass<Book>(Books)
//
//    var writer by Writer referencedOn Books.writer
//    var title by Books.title
//    var price by Books.price
//    var createdAt by Books.createdAt
//    var updatedAt by Books.updatedAt
//
//    override fun toString(): String {
//        return "Book(writer=$writer, title='$title', price=$price, createdAt=$createdAt, updatedAt=$updatedAt)"
//    }
//}
//
//class Writer(id: EntityID<Long>) : LongEntity(id) {
//    companion object : LongEntityClass<Writer>(Writers)
//
//    var name by Writers.name
//        private set
//    var email by Writers.email
//        private set
//    var createdAt by Writers.createdAt
//        private set
//    var updatedAt by Writers.updatedAt
//        private set
//
//    fun updateProfile(name: String, email: String) {
//        this.name = name
//        this.email = email
//        this.updatedAt = LocalDateTime.now()
//    }
//
//    fun register(name: String, email: String) {
//        this.name = name
//        this.email = email
//        this.updatedAt = LocalDateTime.now()
//        this.createdAt = LocalDateTime.now()
//    }
//
//    override fun toString(): String {
//        return "Writer(name='$name', email='$email', createdAt=$createdAt, updatedAt=$updatedAt)"
//    }
//}
