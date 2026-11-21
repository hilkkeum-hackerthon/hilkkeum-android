package kr.co.cmc.hil.domain.common

/**
 * 성공 값을 [transform]으로 변환한다. 실패면 에러를 그대로 전달한다.
 */
inline fun <T, E : AppError, R> AppResult<T, E>.map(
    transform: (T) -> R,
): AppResult<R, E> {
    return when (this) {
        is AppResult.Error -> AppResult.Error(error)
        is AppResult.Success -> AppResult.Success(transform(data))
    }
}

/**
 * 성공일 때만 [action]을 실행하고, 체이닝을 위해 자기 자신을 반환한다.
 */
inline fun <T, E : AppError> AppResult<T, E>.onSuccess(
    action: (T) -> Unit,
): AppResult<T, E> {
    if (this is AppResult.Success) action(data)
    return this
}

/**
 * 실패일 때만 [action]을 실행하고, 체이닝을 위해 자기 자신을 반환한다.
 */
inline fun <T, E : AppError> AppResult<T, E>.onFailure(
    action: (E) -> Unit,
): AppResult<T, E> {
    if (this is AppResult.Error) action(error)
    return this
}

/**
 * 성공 값을 버리고 [EmptyAppResult]로 변환한다. 성공/실패 여부만 필요할 때 사용한다.
 */
fun <T, E : AppError> AppResult<T, E>.asEmptyResult(): EmptyAppResult<E> {
    return map { }
}
