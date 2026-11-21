package kr.co.cmc.hil.data.repositoryimpl

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kr.co.cmc.hil.data.datastore.TokenStorage
import kr.co.cmc.hil.data.datastore.withMemberToken
import kr.co.cmc.hil.data.dto.coupon.CouponUseRequestResponseDto
import kr.co.cmc.hil.data.dto.coupon.CouponsResponseDto
import kr.co.cmc.hil.data.dto.coupon.ExchangeCouponResponseDto
import kr.co.cmc.hil.data.dto.coupon.IncomingCouponResponseDto
import kr.co.cmc.hil.data.dto.coupon.RespondCouponUseRequestDto
import kr.co.cmc.hil.data.dto.coupon.RespondCouponUseResponseDto
import kr.co.cmc.hil.data.mapper.toDomain
import kr.co.cmc.hil.data.safeApiCall
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.common.map
import kr.co.cmc.hil.domain.model.coupon.Coupon
import kr.co.cmc.hil.domain.model.coupon.CouponId
import kr.co.cmc.hil.domain.model.coupon.CouponUseRequest
import kr.co.cmc.hil.domain.model.coupon.CouponUseResponse
import kr.co.cmc.hil.domain.model.coupon.ExchangedCoupon
import kr.co.cmc.hil.domain.model.coupon.IncomingCouponRequest
import kr.co.cmc.hil.domain.repository.CouponRepository
import javax.inject.Inject

class CouponRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
) : CouponRepository {

    /**
     * `POST api/v1/coupons/exchange`를 호출하고 응답을 [ExchangedCoupon]으로 변환한다.
     */
    override suspend fun exchangeCoupon(): AppResult<ExchangedCoupon, AppError.Network> {
        return tokenStorage.withMemberToken { token ->
            safeApiCall<ExchangeCouponResponseDto> {
                httpClient.post("api/v1/coupons/exchange") { bearerAuth(token) }
            }.map { it.toDomain() }
        }
    }

    /**
     * `GET api/v1/coupons`를 호출하고 응답에서 보유 중인 [Coupon]을 꺼낸다.
     */
    override suspend fun getCoupon(): AppResult<Coupon?, AppError.Network> {
        return tokenStorage.withMemberToken { token ->
            safeApiCall<CouponsResponseDto> {
                httpClient.get("api/v1/coupons") { bearerAuth(token) }
            }.map { it.toDomain() }
        }
    }

    /**
     * `POST api/v1/coupons/{couponId}/use`를 호출하고 응답을 [CouponUseRequest]로 변환한다.
     */
    override suspend fun requestCouponUse(couponId: CouponId): AppResult<CouponUseRequest, AppError.Network> {
        return tokenStorage.withMemberToken { token ->
            safeApiCall<CouponUseRequestResponseDto> {
                httpClient.post("api/v1/coupons/${couponId.value}/use") { bearerAuth(token) }
            }.map { it.toDomain() }
        }
    }

    /**
     * `POST api/v1/coupons/{couponId}/respond`를 호출하고 응답을 [CouponUseResponse]로 변환한다.
     */
    override suspend fun respondCouponUse(
        couponId: CouponId,
        approved: Boolean,
    ): AppResult<CouponUseResponse, AppError.Network> {
        return tokenStorage.withMemberToken { token ->
            safeApiCall<RespondCouponUseResponseDto> {
                httpClient.post("api/v1/coupons/${couponId.value}/respond") {
                    bearerAuth(token)
                    setBody(RespondCouponUseRequestDto(approved = approved))
                }
            }.map { it.toDomain() }
        }
    }

    /**
     * `GET api/v1/coupons/incoming`(G8 제안 엔드포인트)을 호출하고 받은 요청을 [IncomingCouponRequest]로 변환한다.
     * 서버에 엔드포인트가 아직 없어 404가 오면 받은 요청이 없는 것으로 처리한다.
     */
    override suspend fun getIncomingCouponRequest(): AppResult<IncomingCouponRequest?, AppError.Network> {
        val result = tokenStorage.withMemberToken { token ->
            safeApiCall<IncomingCouponResponseDto?> {
                httpClient.get("api/v1/coupons/incoming") { bearerAuth(token) }
            }.map { it?.toDomain() }
        }

        return if (result is AppResult.Error && result.error == AppError.Network.NOT_FOUND) {
            AppResult.Success(null)
        } else {
            result
        }
    }
}
