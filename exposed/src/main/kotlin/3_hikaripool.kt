import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

/**
 * @author Larry
 */
fun main() {
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:h2:mem:test"
        driverClassName = "org.h2.Driver"
        validate()
    }
    val dataSource: DataSource = HikariDataSource(config)
    val database = Database.connect(dataSource)

    // multi database 사용 가능
    transaction(database) {
        addLogger(StdOutSqlLogger)

        SchemaUtils.create(Users)

        val id = Users.insertAndGetId {
            it[name] = "larry.charry"
        }

        println("id: $id")
    }
}
