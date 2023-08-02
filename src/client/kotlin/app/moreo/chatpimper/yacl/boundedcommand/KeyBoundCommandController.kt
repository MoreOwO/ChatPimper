package app.moreo.chatpimper.yacl.boundedcommand

import app.moreo.chatpimper.config.KeyBoundCommand
import dev.isxander.yacl3.api.Controller
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.controller.ControllerBuilder
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.AbstractWidget
import dev.isxander.yacl3.gui.YACLScreen
import net.minecraft.text.Text

class KeyBoundCommandController(private val option: Option<KeyBoundCommand>): Controller<KeyBoundCommand> {

    class KeyBoundCommandControllerBuilder(private val option: Option<KeyBoundCommand>): ControllerBuilder<KeyBoundCommand> {
        companion object {
            @Suppress("unused")
            @JvmStatic
            fun create(option: Option<KeyBoundCommand>): KeyBoundCommandControllerBuilder {
                return KeyBoundCommandControllerBuilder(option)
            }
        }

        override fun build(): Controller<KeyBoundCommand> {
            return KeyBoundCommandController(option)
        }
    }

    var formattedValue: Text = Text.literal(option.pendingValue().command) ?: Text.of("/")

    override fun option(): Option<KeyBoundCommand> {
        return option
    }

    override fun formatValue(): Text {
        return formattedValue
    }

    override fun provideWidget(screen: YACLScreen, widgetDimension: Dimension<Int>): AbstractWidget {
        return KeyBoundCommandControllerElement(this, screen, widgetDimension)
    }
}