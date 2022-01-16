class PayServiceActResult(
    private val paymentAdapter: PaymentAdapter
) {

    fun doPay(req: PayRequest): ActResult<Unit> =
        (if (isNew(req)) {
            pay(req)
        } else {
            act { paymentAdapter.payQuery(req) }
                .flatMap { isPaid ->
                    if (isPaid) {
                        markSuccess(req)
                        ActResult.success<Unit>()
                    } else {
                        pay(req)
                    }
                }
                .onSuccess { println("query success.") }
                .onFailure { println("query failure.") }
                .onUnknown { println("query unknown.") }

        })
            .onSuccess { println("doPay success.") }
            .onFailure { println("doPay success.") }
            .onUnknown { println("doPay unknown.") }

    private fun markSuccess(req: PayRequest) {
        println("mark Success.")
    }

    private fun pay(req: PayRequest): ActResult<Unit> =
        act { paymentAdapter.pay(req) }
            .onSuccess { println("payment success.") }
            .onFailure { println("payment failed.") }
            .onUnknown { println("payment unknown.") }
}
