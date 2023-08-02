package app.moreo.chatpimper.config

import com.google.gson.*
import java.lang.reflect.Type

data class KeyBoundCommand(val key: Int, val special: Int?, val command: String) {

    class Serializer: JsonSerializer<KeyBoundCommand>, JsonDeserializer<KeyBoundCommand> {
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): KeyBoundCommand? {
            if (!json.isJsonObject) {
                return null
            }

            val key = json.asJsonObject.get("key").asInt
            val special = json.asJsonObject.get("special")?.asInt
            val command = json.asJsonObject.get("command").asString

            return KeyBoundCommand(key, special, command)
        }

        override fun serialize(src: KeyBoundCommand, typeOfSrc: Type?, context: JsonSerializationContext): JsonElement {
            val obj = JsonObject()
            obj.add("key", context.serialize(src.key))
            obj.add("special", context.serialize(src.special))
            obj.add("command", context.serialize(src.command))

            return obj
        }
    }
}
