package app.moreo.chatpimper

import app.moreo.chatpimper.config.Config
import app.moreo.chatpimper.utils.*
import app.moreo.ucl.minecraft.ChatColorFormat
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.ColorControllerBuilder
import dev.isxander.yacl3.api.controller.EnumControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import net.minecraft.text.Text
import java.awt.Color
import java.util.function.Consumer
import java.util.function.Supplier

class ModMenuIntegration: ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {

        val config = Config.get("default")
        val builder = YetAnotherConfigLib.createBuilder()

        builder.save(config::save)

        builder.title(Text.translatable("config.title")).category {
            name(Text.translatable("config.category.global"))

            group {
                name(Text.translatable("config.group.messages"))

                option<String> {
                    name(Text.translatable("config.param.messages.prefix"))
                    binding {
                        def = config.defaults.messagePrefix
                        getter = Supplier { config.config.messagePrefix }
                        setter = Consumer { config.config.messagePrefix = it }
                    }

                    controller(StringControllerBuilder::create)
                }

                option<String> {
                    name(Text.translatable("config.param.messages.suffix"))
                    binding {
                        def = config.defaults.messageSuffix
                        getter = Supplier { config.config.messageSuffix }
                        setter = Consumer { config.config.messageSuffix = it }
                    }

                    controller(StringControllerBuilder::create)
                }
            }

            group {
                name(Text.translatable("config.group.gradient"))
                val gradientEnabled = option<Boolean> {
                    name(Text.translatable("config.param.gradient.toggle"))
                    binding {
                        def = config.defaults.colorGradient
                        getter = Supplier { config.config.colorGradient }
                        setter = Consumer { config.config.colorGradient = it }
                    }
                    controller {
                        BooleanControllerBuilder.create(it).onOffFormatter().coloured(true)
                    }
                }

                val useCommand = option<Boolean> {
                    name(Text.translatable("config.param.gradient.enableWithCommand"))
                    binding {
                        def = config.defaults.useCommandToEnable
                        getter = Supplier { config.config.useCommandToEnable }
                        setter = Consumer { config.config.useCommandToEnable = it }
                    }

                    controller {
                        BooleanControllerBuilder.create(it).yesNoFormatter().coloured(true)
                    }

                    available(config.config.colorGradient)
                }.apply {
                    gradientEnabled.addListener { _, u ->
                        this.setAvailable(u)
                    }
                }

                option<String> {
                    name(Text.translatable("config.param.gradient.enableCommand"))
                    binding {
                        def = config.defaults.colorGradientEnableCommand
                        getter = Supplier { config.config.colorGradientEnableCommand }
                        setter = Consumer { config.config.colorGradientEnableCommand = it }
                    }

                    controller(StringControllerBuilder::create)
                    available(config.config.colorGradient && config.config.useCommandToEnable)
                }.apply {
                    gradientEnabled.addListener { _, u ->
                        this.setAvailable(u && useCommand.pendingValue())
                    }

                    useCommand.addListener { _, u ->
                        this.setAvailable(u && gradientEnabled.pendingValue())
                    }
                }

                option<Color> {
                    name(Text.translatable("config.param.gradient.colorStart"))
                    binding {
                        def = config.defaults.colorGradientStart.toAWTColor()
                        getter = Supplier { config.config.colorGradientStart.toAWTColor() }
                        setter = Consumer { config.config.colorGradientStart = it.toSRGBColor() }
                    }

                    controller {
                        ColorControllerBuilder.create(it).allowAlpha(false)
                    }

                    available(config.config.colorGradient)
                }.apply {
                    gradientEnabled.addListener { _, u ->
                        this.setAvailable(u)
                    }
                }

                option<Color> {
                    name(Text.translatable("config.param.gradient.colorEnd"))
                    binding {
                        def = config.config.colorGradientEnd.toAWTColor()
                        getter = Supplier { config.config.colorGradientEnd.toAWTColor() }
                        setter = Consumer { config.config.colorGradientEnd = it.toSRGBColor() }
                    }

                    controller {
                        ColorControllerBuilder.create(it).allowAlpha(false)
                    }

                    available(config.config.colorGradient)
                }.apply {
                    gradientEnabled.addListener { _, u ->
                        this.setAvailable(u)
                    }
                }

                option<ChatColorFormat> {
                    name(Text.translatable("config.param.gradient.chatColorFormat"))
                    binding {
                        def = config.config.colorGradientFormat
                        getter = Supplier { config.config.colorGradientFormat }
                        setter = Consumer { config.config.colorGradientFormat = it }
                    }

                    controller {
                        EnumControllerBuilder.create(it).enumClass(ChatColorFormat::class.java).valueFormatter { v ->
                            Text.translatable("config.enum.chatColorFormat.${v.name.lowercase()}")
                        }
                    }

                    available(config.config.colorGradient)
                }.apply {
                    gradientEnabled.addListener { _, u ->
                        this.setAvailable(u)
                    }
                }
            }
        }


        return ConfigScreenFactory { parent ->
            builder.build().generateScreen(parent)
        }
    }
}