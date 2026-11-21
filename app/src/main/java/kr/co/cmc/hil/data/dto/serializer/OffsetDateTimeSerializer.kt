package kr.co.cmc.hil.data.dto.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

/**
 * ISO-8601 오프셋 시각 문자열(예: `2026-11-21T20:00:00+09:00`)과 [OffsetDateTime]을 변환한다.
 * 형식이 맞지 않으면 [SerializationException]을 던져 `safeApiCall`이 SERIALIZATION 에러로 돌려주게 한다.
 */
object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.time.OffsetDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        val value = decoder.decodeString()
        return try {
            OffsetDateTime.parse(value)
        } catch (e: DateTimeParseException) {
            throw SerializationException("Invalid OffsetDateTime: $value", e)
        }
    }

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.toString())
    }
}
