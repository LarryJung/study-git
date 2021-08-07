package com.example.routingdatasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.sql.DataSource


enum class DbLookupKey {
    MASTER, SLAVE
}

val dbConfig1 = HikariConfig().apply {
    jdbcUrl = "jdbc:mysql://localhost:3377/exposed_db?useSSL=false&serverTimezone=Asia/Seoul&autoReconnect=true"
    driverClassName = "com.mysql.cj.jdbc.Driver"
    username = "root"
    password = ""
    isReadOnly = false
}

val dbConfig2 = HikariConfig().apply {
    jdbcUrl = "jdbc:mysql://localhost:3388/exposed_db?useSSL=false&serverTimezone=Asia/Seoul&autoReconnect=true"
    driverClassName = "com.mysql.cj.jdbc.Driver"
    username = "root"
    password = ""
    isReadOnly = true
}

val dataSourceMap: Map<DbLookupKey, DataSource> = mapOf(
    DbLookupKey.MASTER to HikariDataSource(dbConfig1),
    DbLookupKey.SLAVE to HikariDataSource(dbConfig2),
)

interface DetermineDbKeyStrategy {
    fun getKey(): DbLookupKey
}

class OnlyMasterDecider : DetermineDbKeyStrategy {
    override fun getKey(): DbLookupKey {
        return DbLookupKey.MASTER.also {
            println("$it is selected")
        }
    }
}

class ReadOnlyDecider : DetermineDbKeyStrategy {
    override fun getKey(): DbLookupKey =
        (if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) DbLookupKey.SLAVE
        else DbLookupKey.MASTER).also {
            println("$it is selected")
        }
}

class DynamicRoutingDataSource(private val strategy: DetermineDbKeyStrategy) : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any {
        return strategy.getKey()
    }
}

@Configuration
class DbConfig {

    @Bean
    fun determineDbKeyStrategy(): DetermineDbKeyStrategy = ReadOnlyDecider()

    @Bean
    fun routingDataSource(determineDbKeyStrategy: DetermineDbKeyStrategy): DataSource {
        return DynamicRoutingDataSource(determineDbKeyStrategy)
            .apply {
                setTargetDataSources(dataSourceMap.toMap())
                setDefaultTargetDataSource(
                    dataSourceMap[DbLookupKey.MASTER]
                        ?: throw RuntimeException("no datasource")
                )
            }
    }

    @Primary
    @DependsOn("routingDataSource")
    @Bean
    fun dataSource(routingDataSource: DataSource): DataSource {
        return LazyConnectionDataSourceProxy(routingDataSource)
    }

}
