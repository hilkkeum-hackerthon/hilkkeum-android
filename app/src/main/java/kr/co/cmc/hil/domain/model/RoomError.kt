package kr.co.cmc.hil.domain.model

import kr.co.cmc.hil.domain.common.AppError

enum class RoomError : AppError {
    MissingServer, ValidationFailed, Unauthorized, PermissionDenied, NotFound, RoomFull,
    NetworkError, ServerError, InvalidResponse, StorageError,
}
