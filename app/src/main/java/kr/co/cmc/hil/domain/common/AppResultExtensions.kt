package kr.co.cmc.hil.domain.common

inline fun <T, E : AppError, R> AppResult<T, E>.map(
    transform: (T) -> R,
): AppResult<R, E> {
    return when (this) {
        is AppResult.Error -> AppResult.Error(error)
        is AppResult.Success -> AppResult.Success(transform(data))
    }
}

inline fun <T, E : AppError> AppResult<T, E>.onSuccess(
    action: (T) -> Unit,
): AppResult<T, E> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T, E : AppError> AppResult<T, E>.onFailure(
    action: (E) -> Unit,
): AppResult<T, E> {
    if (this is AppResult.Error) action(error)
    return this
}

fun <T, E : AppError> AppResult<T, E>.asEmptyResult(): EmptyAppResult<E> {
    return map { }
}
