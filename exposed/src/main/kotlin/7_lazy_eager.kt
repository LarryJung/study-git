import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils

/**
 * @author Larry
 */
fun main() {
    val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    val txHandler = TxHandlerImpl(db)

    txHandler.tx {
        SchemaUtils.create(GiftUsers, Givers, Gifts)
    }

    txHandler.tx {
        repeat(10) {
            GiftUser.new {
                accountId = it
                available = true
            }
        }

        val giverGiftUser = GiftUser.new {
            accountId = 100
            available = true
        }
        val giver = Giver.new {
            giftUser = giverGiftUser
        }
        repeat(10) {
            Gift.new {
                giftUser = GiftUser[it.toLong() + 1]
                this.giver = giver
                giftStatus = GiftStatus.CREATED
                giftSerialId = "serial"
                balance = 1000
            }
        }
    }

    println("lazy loading ====")
    txHandler.tx {
        val gift = Gift[1L]
        println(gift.giver)
        println(gift.giver.giftUser)
    }

    println("eager loading 단건 ==== join query 가 아닌 엔티티 조회 시점에 연관 객체까지 로딩한다는 의미의 eager")
    txHandler.tx {
        val gift = Gift[1L].load(Gift::giftUser, Gift::giver, Giver::giftUser)
        println(gift.giver)
        println(gift.giver)
        println(gift.giver.giftUser)
    }

    println("eager loading 여러건 ==== 다건 쿼리에 붙어있는 연관 객체들은 in 절 query 를 한번 더 수행한다. n+1 방지 ")
    txHandler.tx {
        println(Gift.all().with(Gift::giftUser, Gift::giver, Giver::giftUser).map { it.giftUser.accountId })
    }
}
