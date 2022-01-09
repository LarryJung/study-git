import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author Larry
 */
fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(GiftUsers, Givers, Gifts)
    }
}

// === table config. dsl 에 활용. 복수 컨벤션 ==============================================================================
object GiftUsers : BaseLongIdTable("gift_user", "gift_user_id") {
    val accountId = integer("account_id")
    val available = bool("available")
}

object Givers : BaseLongIdTable("giver", "giver_id") {
    val giftUserId = reference("gift_user_id", GiftUsers.id, onDelete = null, onUpdate = null).nullable()
    var partnerId = long("partner_id").nullable()
}

object Gifts : BaseLongIdTable("gift", "gift_id") {
    val giftUserId = reference("gift_user_id", GiftUsers.id, onDelete = null, onUpdate = null)
    val giverId = reference("giver_id", Givers.id)
    val giftStatus = enumerationByName("gift_status", 50, GiftStatus::class)
    val giftSerialId = varchar("gift_serial_id", 50)
    val balance = integer("balance")
    val issuedDate = date("issued_date").clientDefault { LocalDate.now() }
}


// === dao config. 단수 컨벤션. var 로만.. =================================================================================
class GiftUser(id: EntityID<Long>) : BaseEntity(id, GiftUsers) {
    companion object : BaseEntityClass<GiftUser>(GiftUsers)

    var accountId by GiftUsers.accountId
    var available by GiftUsers.available
}

class Giver(id: EntityID<Long>) : BaseEntity(id, Givers) {
    companion object : BaseEntityClass<Giver>(Givers)

    var giftUser by GiftUser optionalReferencedOn Givers.giftUserId
    var partnerId by Givers.partnerId
}

class Gift(id: EntityID<Long>) : BaseEntity(id, Gifts) {
    companion object : BaseEntityClass<Gift>(Gifts)

    var giftUser by GiftUser referencedOn Gifts.giftUserId
    var giver by Giver referencedOn Gifts.giverId
    var giftStatus by Gifts.giftStatus
    var giftSerialId by Gifts.giftSerialId
    var balance by Gifts.balance
    var issuedDate by Gifts.issuedDate
}


// === UTILS ===========================================================================================================

abstract class BaseLongIdTable(name: String, idName: String = "id") : LongIdTable(name, idName) {
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

abstract class BaseEntity(id: EntityID<Long>, table: BaseLongIdTable) : LongEntity(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt
}

abstract class BaseEntityClass<E : BaseEntity>(table: BaseLongIdTable) : LongEntityClass<E>(table) {

    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                try {
                    action.toEntity(this)?.updatedAt = LocalDateTime.now()
                } catch (e: Exception) {
                    //nothing much to do here
                }
            }
        }
    }
}

val BaseEntity.idValue: Long
    get() = this.id.value


enum class GiftStatus(val description: String) {
    CREATED("생성"),
    PUBLISHED("발행"),
    CANCELED("발행 취소"),
    RECEIVED("수락"),

    EXPIRED_REFUNDED("유효기간 만료 환불"),
    REQUESTED_REFUNDED("사용자 요청 잔액 환불"),
    NON_RECEIPT_REFUNDED("미수신 환불"),

    PAYMENT_FAILED("결제 실패"),
    PAYMENT_PENDING("결제 확인 중"),

    ADMIN_FORCE_REFUNDED("어드민 강제 만료(머니환불)"),
    ADMIN_MANUAL_REFUNDED("어드민 강제 만료(수기환불)");
}
