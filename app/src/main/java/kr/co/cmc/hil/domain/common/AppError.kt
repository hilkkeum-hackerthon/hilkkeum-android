package kr.co.cmc.hil.domain.common

/**
 * 앱 전역 에러의 최상위 타입.
 * 기능별 에러(예: 검증 에러)는 이 인터페이스를 구현한 enum/sealed 타입으로 정의한다.
 */
interface AppError {

    enum class Network : AppError {
        /** 400 */
        BAD_REQUEST,

        /** 401 */
        UNAUTHORIZED,

        /** 403 */
        FORBIDDEN,

        /** 404 */
        NOT_FOUND,

        /** 408 */
        REQUEST_TIMEOUT,

        /** 409 */
        CONFLICT,

        /** 413 */
        PAYLOAD_TOO_LARGE,

        /** 429 */
        TOO_MANY_REQUESTS,

        /** 500..599 (503 제외) */
        SERVER_ERROR,

        /** 503 */
        SERVICE_UNAVAILABLE,

        /** 네트워크 연결 없음 */
        NO_INTERNET,

        /** 응답 파싱 실패 */
        SERIALIZATION,

        UNKNOWN,
    }
}
