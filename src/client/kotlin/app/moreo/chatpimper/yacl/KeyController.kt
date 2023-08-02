package app.moreo.chatpimper.yacl

import dev.isxander.yacl3.api.Controller
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.AbstractWidget
import dev.isxander.yacl3.gui.YACLScreen
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text

class KeyController(private val option: Option<InputUtil.Key>): Controller<InputUtil.Key>  {

    private var formattedValue: Text = option.pendingValue()?.localizedText ?: InputUtil.UNKNOWN_KEY.localizedText

    override fun option(): Option<InputUtil.Key> {
        return option
    }

    override fun formatValue(): Text {
        return formattedValue
    }

    fun setFormattedValue(text: Text) {
        formattedValue = text
    }

    override fun provideWidget(screen: YACLScreen, widgetDimension: Dimension<Int>): AbstractWidget {
        return KeyControllerElement(this, screen, widgetDimension)
    }
}