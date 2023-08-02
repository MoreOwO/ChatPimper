package app.moreo.chatpimper.yacl

import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.controllers.ControllerWidget
import net.minecraft.client.util.InputUtil
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text

class KeyControllerElement(control: KeyController, screen: YACLScreen, dims: Dimension<Int>): ControllerWidget<KeyController>(control, screen, dims) {

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!this.isFocused) return false
        this.isFocused = false
        val key = if (keyCode == InputUtil.GLFW_KEY_ESCAPE) {
            InputUtil.UNKNOWN_KEY
        } else {
            InputUtil.fromKeyCode(keyCode, scanCode)
        }
        control.option().requestSet(key)
        control.setFormattedValue(key.localizedText)
        return true
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isAvailable && dimension.isPointInside(mouseX.toInt(), mouseY.toInt())) {
            this.isFocused = true
            control.setFormattedValue(getFocusedText(control.option().pendingValue().code))
            return true
        }

        return false
    }

    private fun getFocusedText(keyCode: Int): Text {
        return Text.literal("[ ").setStyle(Style.EMPTY.withColor(0xFF5555)).append(MutableText.of(InputUtil.fromKeyCode(keyCode, 0).localizedText.content).setStyle(Style.EMPTY.withColor(0xFFFFFF))).append(Text.literal(" ]").setStyle(Style.EMPTY.withColor(0xFF5555)))
    }

    override fun getHoveredControlWidth(): Int {
        if (this.isFocused) {
            return client.textRenderer.getWidth(getFocusedText(control.option().pendingValue().code))
        }
        return client.textRenderer.getWidth(control.option().pendingValue().localizedText)
    }

}