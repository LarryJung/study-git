import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * @author Larry
 */
interface TxHandler {
    fun <T> tx(block: Transaction.() -> T): T
}

class TxHandlerImpl(private val database: Database) : TxHandler {

    override fun <T> tx(block: Transaction.() -> T): T =
        transaction(database) {
            addLogger(StdOutSqlLogger)
            block(this)
        }
}
