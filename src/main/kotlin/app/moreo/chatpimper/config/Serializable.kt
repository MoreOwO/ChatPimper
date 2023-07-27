package app.moreo.chatpimper.config

import app.moreo.ucl.colors.XYZD65Color
import app.moreo.ucl.minecraft.ChatColorFormat
import app.moreo.ucl.serialization.XYZD65ColorSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val configJsonSerializer = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
}

@Serializable
data class SerializableConfig(
    val useAmpersandAsColorCharacter: Boolean = false,
    val messagePrefix: String = "",
    val messageSuffix: String = "",
    val colorGradient: Boolean = false,
    val colorGradientEnableCommand: String = "#gradient",
    @Serializable(with = XYZD65ColorSerializer::class) val colorGradientStart: XYZD65Color = XYZD65Color.D65_REFERENCE_WHITE,
    @Serializable(with = XYZD65ColorSerializer::class) val colorGradientEnd: XYZD65Color = XYZD65Color.D65_REFERENCE_WHITE,
    val colorGradientFormat: ChatColorFormat = ChatColorFormat.AMPERSAND
)