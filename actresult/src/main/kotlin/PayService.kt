import java.net.SocketTimeoutException

class PayService(
    private val paymentAdapter: PaymentAdapter
) {

    fun doPay_1(req: PayRequest) {
        try {
            paymentAdapter.pay(req)
        } catch (e: RuntimeException) {
            println("payment failed.")
        }
    }

    fun doPay_2(req: PayRequest) {
        try {
            paymentAdapter.pay(req)
        } catch (e: Throwable) {
            if (isFailedException(e)) {
                println("payment failed.")
            } else {
                println("payment unknown. retry available")
            }
        }
    }

    fun doPay_3(req: PayRequest) {
        if (isNew(req)) {
            pay(req)
        } else {
            try {
                val isPaid = paymentAdapter.payQuery(req)
                if (isPaid) {
                    markSuccess(req)
                } else {
                    pay(req)
                }
            } catch (e: Throwable) {
                if (isFailedException(e)) {
                    println("exist payment. but unknown result. query failed")
                } else {
                    println("exist payment. but unknown result. query unknown")
                }
            }
        }
    }

    private fun markSuccess(req: PayRequest) {}

    private fun pay(req: PayRequest) {
        try {
            paymentAdapter.pay(req)
        } catch (e: Throwable) {
            if (isFailedException(e)) {
                println("payment failed.")
            } else {
                println("payment unknown.")
            }
        }
    }
}

data class PayRequest(val id: Long)

fun isFailedException(e: Throwable): Boolean = when (e) {
    is RuntimeException -> true
    is SocketTimeoutException -> false
    else -> {
        println("unknown exception. $e")
        false
    }
}

fun isNew(req: PayRequest) = true
