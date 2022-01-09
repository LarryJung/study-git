import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * @author Larry
 */

fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.create(Users)

        val id = Users.insertAndGetId {
            it[name] = "larry.charry"
        }

        println("id: $id")
        // Query instance
        println("${Users.selectAll()}")
        // show query
        println(Users.selectAll().prepareSQL(this))
        println("${Users.selectAll().map { it[Users.name] }}")
    }
}

object Users : IntIdTable() {
    val name = varchar("name", 50)
}
