sealed class ActResult<out A> {
    abstract val resultType: ResultType

    internal class Success<A>(val data: A) : ActResult<A>() {
        override val resultType = ResultType.SUCCESS
    }

    internal class Failure<A>(
        val errorResponse: ErrorResponse
    ) : ActResult<A>() {
        override val resultType = ResultType.FAILURE
    }

    internal class Unknown<A>(
        val errorResponse: ErrorResponse
    ) : ActResult<A>() {
        override val resultType = ResultType.UNKNOWN
    }

    fun getOrElse(defaultValue: () -> @UnsafeVariance A): A = when (this) {
        is Success -> data
        is Failure -> defaultValue()
        is Unknown -> defaultValue()
    }

    fun forEach(
        onSuccess: (A) -> Unit = {},
        onFailure: (ErrorResponse) -> Unit = {},
        onUnknown: (ErrorResponse) -> Unit = {}
    ): Unit = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(errorResponse)
        is Unknown -> onUnknown(errorResponse)
    }

    fun onFailure(f: (ErrorResponse) -> Unit): ActResult<A> = when (this) {
        is Success -> success(data)
        is Failure -> {
            f(errorResponse)
            this
        }
        is Unknown -> this
    }

    fun onUnknown(f: (ErrorResponse) -> Unit): ActResult<A> = when (this) {
        is Success -> success(data)
        is Failure -> this
        is Unknown -> {
            f(errorResponse)
            this
        }
    }

    fun onSuccess(f: (A) -> Unit): ActResult<A> = when (this) {
        is Success -> {
            f(data)
            success(data)
        }
        is Failure -> failure(errorResponse)
        is Unknown -> unknown(errorResponse)
    }

    fun successIf(condition: (ErrorResponse) -> Boolean): ActResult<Unit> =
        when (this) {
            is Success -> success<Unit>()
            is Failure ->
                if (condition(errorResponse)) {
                    success<Unit>()
                } else {
                    failure(errorResponse)
                }
            is Unknown -> unknown(errorResponse)
        }

    fun successIf(
        condition: (ErrorResponse) -> Boolean,
        defaultValue: @UnsafeVariance A
    ): ActResult<A> =
        when (this) {
            is Success -> success(data)
            is Failure ->
                if (condition(errorResponse)) {
                    success(defaultValue)
                } else {
                    failure(errorResponse)
                }
            is Unknown -> unknown(errorResponse)
        }

    // Success 인 경우만 적용됨
    fun <C> map(
        f: (A) -> C
    ): ActResult<C> = when (this) {
        is Success -> Success(f(data))
        is Failure -> failure(errorResponse)
        is Unknown -> unknown(errorResponse)
    }

    fun <C> flatMap(
        f: (A) -> ActResult<C>,
    ): ActResult<C> = when (this) {
        is Success -> f(data)
        is Failure -> failure(errorResponse)
        is Unknown -> unknown(errorResponse)
    }

    // 각 경우에 모두 적용하는 flatMap. 실행되는 함수에 따라서 failure -> success 로 바뀔 수 있음.
    fun <C> forFlatMap(
        onSuccess: (A) -> ActResult<C>,
        onFailure: (ErrorResponse) -> ActResult<C> = { failure(it) },
        onUnknown: (ErrorResponse) -> ActResult<C> = { unknown(it) },
    ): ActResult<C> = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(errorResponse)
        is Unknown -> onUnknown(errorResponse)
    }

    fun recover(
        condition: (ErrorResponse) -> Boolean,
        recover: () -> ActResult<@UnsafeVariance A>
    ): ActResult<A> = when (this) {
        is Success -> success(data)
        is Failure ->
            if (condition(errorResponse)) recover()
            else failure(errorResponse)
        is Unknown -> unknown(errorResponse)
    }

    companion object {
        internal fun <A> success(data: A): ActResult<A> = Success(data)
        internal fun <A> success(): ActResult<Unit> = Success(Unit)
        internal fun <A> failure(errorResponse: ErrorResponse) = Failure<A>(errorResponse)
        internal fun <A> unknown(errorResponse: ErrorResponse) = Unknown<A>(errorResponse)

        operator fun <A> invoke(func: () -> A): ActResult<A> =
            executeExceptionSafeContext(
                run = {
                    Success(func())
                },
                failure = {
                    Failure(it)
                },
                unknown = {
                    Unknown(it)
                }
            )
    }
}

fun <A> act(func: () -> A): ActResult<A> = ActResult { func() }

enum class ResultType {
    SUCCESS, FAILURE, UNKNOWN
}

data class ErrorResponse(val ex: Exception)

