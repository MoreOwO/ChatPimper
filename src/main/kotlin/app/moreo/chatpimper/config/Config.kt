package app.moreo.chatpimper.config

import app.moreo.chatpimper.utils.SRGBSerializer
import app.moreo.ucl.colors.SRGBColor
import app.moreo.ucl.minecraft.ChatColorFormat
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dev.isxander.yacl3.config.ConfigEntry
import dev.isxander.yacl3.config.GsonConfigInstance
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.awt.Color
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

class Config {

    companion object {
        private val configs = mutableMapOf<String, GsonConfigInstance<Config>>()

        private fun getGsonConfigInstance(key: String): GsonConfigInstance<Config> {
            return GsonConfigInstance.createBuilder(Config::class.java)
                .overrideGsonBuilder(
                    GsonBuilder()
                        .registerTypeHierarchyAdapter(Text::class.java, Text.Serializer())
                        .registerTypeHierarchyAdapter(Style::class.java, Style.Serializer())
                        .registerTypeHierarchyAdapter(Color::class.java, GsonConfigInstance.ColorTypeAdapter())
                        .registerTypeHierarchyAdapter(SRGBColor::class.java, SRGBSerializer())
                        .registerTypeHierarchyAdapter(KeyBoundCommand::class.java, KeyBoundCommand.Serializer())
                        .serializeNulls()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                )
                .setPath(Path.of(FabricLoader.getInstance().configDir.toString(), "chat_pimper", "${key}.json").apply { if(!exists()) {
                    createParentDirectories()
                    createFile()
                    writeText("{}")
                } })
                .build().apply {
                    load()
                }
        }

        @JvmStatic
        fun get(key: String): GsonConfigInstance<Config> {
            if (key !in configs) {
                configs[key] = getGsonConfigInstance(key).apply { load() }
            }

            return configs[key]!!
        }
    }

    @ConfigEntry var messagePrefix: String = ""
    @ConfigEntry var messageSuffix: String = ""
    @ConfigEntry var colorGradient: Boolean = false
    @ConfigEntry var colorGradientEnableCommand: String = "#gradient"
    @ConfigEntry var colorGradientStart: SRGBColor = SRGBColor(1f, 1f, 1f)
    @ConfigEntry var colorGradientEnd: SRGBColor = SRGBColor(1f, 1f, 1f)
    @ConfigEntry var colorGradientFormat: ChatColorFormat = ChatColorFormat.AMPERSAND
    @ConfigEntry var useCommandToEnable: Boolean = true
    @ConfigEntry var splitMessages: Boolean = false
    @ConfigEntry var boundedCommands: MutableList<KeyBoundCommand> = mutableListOf()
}