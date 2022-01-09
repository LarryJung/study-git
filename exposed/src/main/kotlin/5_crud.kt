import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * @author Larry
 */
fun main() {
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(GiftUsers, Givers, Gifts)
    }

    // insert
    transaction {
        addLogger(StdOutSqlLogger)
        // dsl
        GiftUsers.insert {
            it[accountId] = 0
            it[available] = true
        }

        // dao
        GiftUser.new {
            accountId = 1
            available = true
        }


    }
    transaction {
        addLogger(StdOutSqlLogger)

        val giftUserByDAO = GiftUser.find { GiftUsers.accountId.eq(1) }.first() // dao, GiftUser
        println(giftUserByDAO.available) // true
    }

    transaction {
        addLogger(StdOutSqlLogger)

        // dsl, ResultRow
        val giftUserByDSL = GiftUsers.select { GiftUsers.accountId.eq(0) }.first()

        // dao, GiftUser
        val giftUserByDAO = GiftUser.find { GiftUsers.accountId.eq(1) }.first()

        println("get dsl id: ${giftUserByDSL[GiftUsers.accountId]}")
        println("get dao id: ${giftUserByDAO.accountId}")

        giftUserByDAO.available = false
    }

    transaction {
        addLogger(StdOutSqlLogger)

        val giftUserByDAO = GiftUser.find { GiftUsers.accountId.eq(1) }.first()
        println(giftUserByDAO.available) // false

        giftUserByDAO.delete()
    }

    transaction {
        addLogger(StdOutSqlLogger)
        GiftUsers.deleteAll()
    }
}
