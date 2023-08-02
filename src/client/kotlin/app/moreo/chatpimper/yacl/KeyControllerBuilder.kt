package app.moreo.chatpimper.yacl

import dev.isxander.yacl3.api.Controller
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.controller.ControllerBuilder
import net.minecraft.client.util.InputUtil


class KeyControllerBuilder(private val option: Option<InputUtil.Key>): ControllerBuilder<InputUtil.Key> {
    companion object {
        @Suppress("unused")
        @JvmStatic
        fun create(option: Option<InputUtil.Key>): KeyControllerBuilder {
            return KeyControllerBuilder(option)
        }
    }

    @Suppress("UnstableApiUsage")
    override fun build(): Controller<InputUtil.Key> {
        return KeyController(option)
    }
}
