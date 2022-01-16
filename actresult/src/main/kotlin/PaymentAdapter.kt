class PaymentAdapter {
    fun pay(req: PayRequest) {}
    fun payQuery(req: PayRequest): Boolean = true
    fun payCancel(req: PayRequest) {}
}
