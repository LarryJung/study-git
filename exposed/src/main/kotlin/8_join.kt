import org.jetbrains.exposed.sql.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author Larry
 */
fun main() {
    val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    val txHandler = TxHandlerImpl(db)
    txHandler.tx {
        SchemaUtils.create(GiftUsers, Givers, Gifts)
    }

    val param = GiftAdminSearchParam(giverAccountId = 0)

    txHandler.tx {

        val qGiver = Givers.alias("gv")
        val qReceiverGiftUser = GiftUsers.alias("rgu")
        val qGiverGiftUser = GiftUsers.alias("ggu")

        val query = Gifts
            .innerJoin(
                qGiver,
                onColumn = { giverId },
                otherColumn = { qGiver[Givers.id] })
            .innerJoin(
                qGiverGiftUser,
                onColumn = { qGiver[Givers.giftUserId] },
                otherColumn = { qGiverGiftUser[GiftUsers.id] })
            .innerJoin(
                qReceiverGiftUser,
                onColumn = { Gifts.giftUserId },
                otherColumn = { qReceiverGiftUser[GiftUsers.id] })
            .slice(
                Gifts.giftSerialId,
                Gifts.balance,
                Gifts.issuedDate,
                qGiverGiftUser[GiftUsers.accountId],
                Expression.build { qGiverGiftUser[GiftUsers.accountId].lessEq(0) },
                qReceiverGiftUser[GiftUsers.accountId],
                qReceiverGiftUser[GiftUsers.accountId],
                Expression.build { qReceiverGiftUser[GiftUsers.accountId].lessEq(0) },
                qReceiverGiftUser[GiftUsers.available],
                Gifts.createdAt
            )
            .selectAll()

        param.giverAccountId?.let {
            query.andWhere { qGiverGiftUser[GiftUsers.accountId].eq(it) }
        }
        param.receiverAccountId?.let {
            query.andWhere { qReceiverGiftUser[GiftUsers.accountId].eq(it) }
        }
        param.giftSerialId?.let {
            query.andWhere { Gifts.giftSerialId.eq(it) }
        }
        param.issuedDateStart?.let {
            query.andWhere { Gifts.issuedDate.greaterEq(it) }
        }
        // it: ResultRow 객체를 맵핑해야한다.
        query.map {
            it[Gifts.id]
        }
    }
}


data class GiftAdminSearchParam(
    val giverAccountId: Int? = null,
    val receiverAccountId: Int? = null,
    val giftSerialId: String? = null,
    val giftStatus: String? = null,
    val issuedDateStart: LocalDate? = null,
    val issuedDateEnd: LocalDate? = null,
    override var page: Int? = 0,
    override var pageSize: Int? = 20,
) : AdminPagingBase


interface AdminPagingBase {
    var page: Int?
    var pageSize: Int?

    fun getOffSet(): Long = (page!! * pageSize!!).toLong()
    fun getLimit(): Long = pageSize!!.toLong()
}

data class AdminGiftDto(
    val giftSerialId: String,
    val balance: Int,
    val giftStatus: GiftStatus,
    val issuedDate: LocalDate,
    val giverAccountId: Int,
    val giverSignOut: Boolean,
    val receiverAccountId: Int,
    val receiverSignOut: Boolean,
    val available: Boolean,
    val createdAt: LocalDateTime,
)
