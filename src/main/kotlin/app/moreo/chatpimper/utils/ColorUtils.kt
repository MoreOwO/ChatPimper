package app.moreo.chatpimper.utils

import app.moreo.ucl.colors.SRGBColor
import com.google.gson.*
import java.awt.Color
import java.lang.reflect.Type

class SRGBSerializer: JsonSerializer<SRGBColor>, JsonDeserializer<SRGBColor>  {
    override fun serialize(src: SRGBColor, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val obj = JsonObject()

        obj.add("r", context!!.serialize(src.red))
        obj.add("g", context.serialize(src.green))
        obj.add("b", context.serialize(src.blue))

        return obj
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): SRGBColor? {
        if (!json.isJsonObject) {
            return null
        }

        val obj = json.asJsonObject

        val red = obj.get("r").asFloat
        val green = obj.get("g").asFloat
        val blue = obj.get("b").asFloat

        return SRGBColor(red, green, blue)
    }
}

fun SRGBColor.toAWTColor(): Color {
    return Color(this.red, this.green, this.blue)
}

fun Color.toSRGBColor(): SRGBColor {
    return SRGBColor.fromInt(this.rgb)
}