package kr.co.cmc.hil.domain.common

enum class AppError {
    MissingServer, ValidationFailed, Unauthorized, PermissionDenied, NotFound, RoomFull,
    NetworkError, ServerError, InvalidResponse, StorageError,
}
