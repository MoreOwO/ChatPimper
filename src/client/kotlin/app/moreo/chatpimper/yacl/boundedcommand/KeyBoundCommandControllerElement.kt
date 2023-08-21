package app.moreo.chatpimper.yacl.boundedcommand


import app.moreo.chatpimper.config.KeyBoundCommand
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.controllers.ControllerWidget
import dev.isxander.yacl3.gui.utils.GuiUtils
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

class KeyBoundCommandControllerElement(control: KeyBoundCommandController, screen: YACLScreen, dims: Dimension<Int>): ControllerWidget<KeyBoundCommandController>(control, screen, dims) {
    private val instantApply = false

    private var inputField: String = control.option().pendingValue().command
    private var inputFieldBounds: Dimension<Int> = dims
    private var inputFieldFocused = false

    private var caretPos = 0
    private var selectionLength = 0

    private var renderOffset = 0

    private val redishStyle = Style.EMPTY.withColor(0xff5555)

    private var ticks = 0f

    private val emptyText: Text = Text.of("")

    private var key: Int = control.option().pendingValue().key
    private var inputFieldKeyPressMode = false

    override fun drawHoveredControl(graphics: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {}

    override fun drawValueText(graphics: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        var valueText = getValueText()
        if (!isHovered) valueText =
            Text.literal(GuiUtils.shortenString(valueText.string, textRenderer, getMaxUnwrapLength(), "..."))
                .setStyle(valueText.style)

        val localizedKeyText = if (key == InputUtil.UNKNOWN_KEY.code) InputUtil.UNKNOWN_KEY.localizedText else InputUtil.fromKeyCode(key, 1).localizedText

        val keyBindText = if (inputFieldKeyPressMode) Text.literal("[ ").setStyle(redishStyle).append(localizedKeyText).append(Text.literal(" ]").setStyle(redishStyle)) else localizedKeyText

        val textX = dimension.xLimit() - textRenderer.getWidth(valueText) + renderOffset - xPadding

        graphics.enableScissor(
            getCorrectedX(),
            inputFieldBounds.y() - 2,
            dimension.xLimit() + 1,
            inputFieldBounds.yLimit() + 4
        )

        graphics.drawText(textRenderer, valueText, textX, textY, valueColor, true)

        var inputColorValue = valueColor

        if (isHovered && !mouseOverLeftHalf(mouseX, mouseY)) {
            ticks += delta
            val text = getValueText().string
            graphics.fill(
                textX,
                inputFieldBounds.yLimit(),
                dimension.xLimit() - xPadding,
                inputFieldBounds.yLimit() + 1,
                -1
            )

            graphics.fill(
                textX + 1,
                inputFieldBounds.yLimit() + 1,
                dimension.xLimit() + 1 - xPadding,
                inputFieldBounds.yLimit() + 2,
                -0xbfbfc0
            )
            if (inputFieldFocused || focused) {
                if (caretPos > text.length) caretPos = text.length
                var caretX = textX + textRenderer.getWidth(text.substring(0, caretPos)) - 1
                if (text.isEmpty()) caretX = inputFieldBounds.x() + inputFieldBounds.width() / 2
                if (ticks % 20 <= 10) {
                    graphics.fill(caretX, inputFieldBounds.y(), caretX + 1, inputFieldBounds.yLimit(), -1)
                }
                if (selectionLength != 0) {
                    val selectionX = textX + textRenderer.getWidth(text.substring(0, caretPos + selectionLength))
                    graphics.fill(
                        caretX,
                        inputFieldBounds.y() - 1,
                        selectionX,
                        inputFieldBounds.yLimit(),
                        -0x7fcfcf01
                    )
                }
            }
        } else if (isHovered && mouseOverLeftHalf(mouseX, mouseY)) {
            inputColorValue = 0xffff55
        }
        graphics.disableScissor()

        graphics.drawText(textRenderer, keyBindText, dimension.x() + xPadding, textY, inputColorValue, true)
    }

    private fun mouseOverLeftHalf(mouseX: Int, mouseY: Int): Boolean {
        return mouseX < dimension.x() + dimension.width() / 2
    }

    private fun getCorrectedX(): Int {
        val localizedKeyText = if (key == InputUtil.UNKNOWN_KEY.code) InputUtil.UNKNOWN_KEY.localizedText else InputUtil.fromKeyCode(key, 1).localizedText

        val keyBindText = if (inputFieldKeyPressMode) Text.literal("[ ").setStyle(redishStyle).append(localizedKeyText).append(Text.literal(" ]").setStyle(redishStyle)) else localizedKeyText

        return dimension.x() + (xPadding * 2) + textRenderer.getWidth(keyBindText)
    }

    private fun getKeyBindWidth(): Int {
        val localizedKeyText = if (key == InputUtil.UNKNOWN_KEY.code) InputUtil.UNKNOWN_KEY.localizedText else InputUtil.fromKeyCode(key, 1).localizedText

        val keyBindText = if (inputFieldKeyPressMode) Text.literal("[ ").setStyle(redishStyle).append(localizedKeyText).append(Text.literal(" ]").setStyle(redishStyle)) else localizedKeyText

        return textRenderer.getWidth(keyBindText) + (xPadding)
    }


    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isAvailable && dimension.isPointInside(mouseX.toInt(), mouseY.toInt()) && !mouseOverLeftHalf(mouseX.toInt(), mouseY.toInt())) {
            inputFieldFocused = true
            if (!inputFieldBounds.isPointInside(mouseX.toInt(), mouseY.toInt())) {
                caretPos = getDefaultCaretPos()
            } else {
                // gets the appropriate caret position for where you click
                var pos = -1
                var currentWidth = 0
                for (ch in inputField.toCharArray()) {
                    pos++
                    val charLength = textRenderer.getWidth(ch.toString())
                    if (currentWidth + charLength / 2 > getCorrectedX()) { // if more than halfway past the characters select in front of that char
                        caretPos = pos
                        break
                    } else if (pos == inputField.length - 1) {
                        // if we have reached the end and no matches, it must be the second half of the char so the last position
                        caretPos = pos + 1
                    }
                    currentWidth += charLength
                }
                selectionLength = 0
            }
            return true
        } else if (mouseOverLeftHalf(mouseX.toInt(), mouseY.toInt()) && isAvailable && dimension.isPointInside(mouseX.toInt(), mouseY.toInt())) {
            inputFieldKeyPressMode = true
            return true
        }

        inputFieldFocused = false
        inputFieldKeyPressMode = false
        return false
    }

    private fun getDefaultCaretPos(): Int {
        return inputField.length
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (inputFieldKeyPressMode) {
            key = if (keyCode == InputUtil.GLFW_KEY_ESCAPE) InputUtil.UNKNOWN_KEY.code else keyCode
            inputFieldKeyPressMode = false
            updateControl()
            return true
        }

        if (!inputFieldFocused) return false

        when (keyCode) {
            InputUtil.GLFW_KEY_ESCAPE, InputUtil.GLFW_KEY_ENTER -> {
                unfocus()
                return true
            }

            InputUtil.GLFW_KEY_LEFT -> {
                if (Screen.hasShiftDown()) {
                    if (Screen.hasControlDown()) {
                        val spaceChar = findSpaceIndex(true)
                        selectionLength += caretPos - spaceChar
                        caretPos = spaceChar
                    } else if (caretPos > 0) {
                        caretPos--
                        selectionLength += 1
                    }
                    checkRenderOffset()
                } else {
                    if (caretPos > 0) {
                        if (selectionLength != 0) {
                            caretPos += min(selectionLength.toDouble(), 0.0).toInt()
                        } else caretPos--
                    }
                    checkRenderOffset()
                    selectionLength = 0
                }
                return true
            }

            InputUtil.GLFW_KEY_RIGHT -> {
                if (Screen.hasShiftDown()) {
                    if (Screen.hasControlDown()) {
                        val spaceChar = findSpaceIndex(false)
                        selectionLength -= spaceChar - caretPos
                        caretPos = spaceChar
                    } else if (caretPos < inputField.length) {
                        caretPos++
                        selectionLength -= 1
                    }
                    checkRenderOffset()
                } else {
                    if (caretPos < inputField.length) {
                        if (selectionLength != 0) caretPos += max(
                            selectionLength.toDouble(),
                            0.0
                        ).toInt() else caretPos++
                        checkRenderOffset()
                    }
                    selectionLength = 0
                }
                return true
            }

            InputUtil.GLFW_KEY_BACKSPACE -> {
                doBackspace()
                return true
            }

            InputUtil.GLFW_KEY_DELETE -> {
                doDelete()
                return true
            }
        }
        if (Screen.isPaste(keyCode)) {
            return doPaste()
        } else if (Screen.isCopy(keyCode)) {
            return doCopy()
        } else if (Screen.isCut(keyCode)) {
            return doCut()
        } else if (Screen.isSelectAll(keyCode)) {
            return doSelectAll()
        }
        return false
    }

    private fun doPaste(): Boolean {
        write(client.keyboard.clipboard)
        return true
    }

    private fun doCopy(): Boolean {
        if (selectionLength != 0) {
            client.keyboard.clipboard = getSelection()
            return true
        }
        return false
    }

    private fun doCut(): Boolean {
        if (selectionLength != 0) {
            client.keyboard.clipboard = getSelection()
            write("")
            return true
        }
        return false
    }

    private fun doSelectAll(): Boolean {
        caretPos = inputField.length
        checkRenderOffset()
        selectionLength = -caretPos
        return true
    }

    private fun checkRenderOffset() {
        if (textRenderer.getWidth(inputField) < getUnshiftedLength()) {
            renderOffset = 0
            return
        }
        val textX = dimension.xLimit() - textRenderer.getWidth(inputField) - xPadding
        val caretX = textX + textRenderer.getWidth(inputField.substring(0, caretPos)) - 1

        val minX = dimension.xLimit() - xPadding - getUnshiftedLength()
        val maxX = minX + getUnshiftedLength()
        if (caretX + renderOffset < minX) {
            renderOffset = minX - caretX
        } else if (caretX + renderOffset > maxX) {
            renderOffset = maxX - caretX
        }
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (!inputFieldFocused) return false
        write(chr.toString())
        return true
    }

    private fun doBackspace() {
        if (selectionLength != 0) {
            write("")
        } else if (caretPos > 0) {
            if (modifyInput { builder: StringBuilder ->
                    builder.deleteCharAt(
                        caretPos - 1
                    )
                }) {
                caretPos--
                checkRenderOffset()
            }
        }
    }

    private fun doDelete() {
        if (selectionLength != 0) {
            write("")
        } else if (caretPos < inputField.length) {
            modifyInput { builder: StringBuilder ->
                builder.deleteCharAt(
                    caretPos
                )
            }
        }
    }

    private fun write(string: String) {
        if (selectionLength == 0) {
            if (modifyInput { builder: StringBuilder ->
                    builder.insert(
                        caretPos,
                        string
                    )
                }) {
                caretPos += string.length
                checkRenderOffset()
            }
        } else {
            val start = getSelectionStart()
            val end = getSelectionEnd()
            if (modifyInput { builder: StringBuilder ->
                    builder.replace(
                        start,
                        end,
                        string
                    )
                }) {
                caretPos = start + string.length
                selectionLength = 0
                checkRenderOffset()
            }
        }
    }

    private fun modifyInput(consumer: Consumer<StringBuilder>): Boolean {
        val temp = StringBuilder(inputField)
        consumer.accept(temp)
        inputField = temp.toString()
        if (instantApply) updateControl()
        return true
    }

    private fun getUnshiftedLength(): Int {
        return if (optionNameString.isEmpty()) dimension.width() - getKeyBindWidth() - xPadding * 2 else dimension.width() / 8 * 5
    }

    private fun getMaxUnwrapLength(): Int {
        return if (optionNameString.isEmpty()) (dimension.width() - (xPadding * 2) - getKeyBindWidth()) else dimension.width() / 2
    }

    private fun getSelectionStart(): Int {
        return min(caretPos.toDouble(), (caretPos + selectionLength).toDouble()).toInt()
    }

    private fun getSelectionEnd(): Int {
        return max(caretPos.toDouble(), (caretPos + selectionLength).toDouble()).toInt()
    }

    private fun getSelection(): String {
        return inputField.substring(getSelectionStart(), getSelectionEnd())
    }

    private fun findSpaceIndex(reverse: Boolean): Int {
        var i: Int
        var fromIndex = caretPos
        if (reverse) {
            if (caretPos > 0) fromIndex -= 1
            i = inputField.lastIndexOf(" ", fromIndex)
            if (i == -1) i = 0
        } else {
            if (caretPos < inputField.length) fromIndex += 1
            i = inputField.indexOf(" ", fromIndex)
            if (i == -1) i = inputField.length
        }
        return i
    }

    override fun setFocused(focused: Boolean) {
        super.setFocused(focused)
        inputFieldFocused = focused
    }

    override fun unfocus() {
        super.unfocus()
        inputFieldFocused = false
        renderOffset = 0
        if (!instantApply) updateControl()
    }

    override fun setDimension(dim: Dimension<Int>) {
        super.setDimension(dim)
        val width =
            max(6.0, min(textRenderer.getWidth(getValueText()).toDouble(), getUnshiftedLength().toDouble()))
                .toInt()
        inputFieldBounds = Dimension.ofInt(
            dimension.xLimit() - xPadding - width ,
            dim.centerY() - textRenderer.fontHeight / 2,
            width,
            textRenderer.fontHeight
        )
    }

    override fun isHovered(): Boolean {
        return super.isHovered() || inputFieldFocused
    }

    private fun updateControl() {
        control.option().requestSet(KeyBoundCommand(key, 0, inputField))
    }

    override fun getUnhoveredControlWidth(): Int {
        return if (!isHovered) min(
            hoveredControlWidth.toDouble(),
            getMaxUnwrapLength().toDouble()
        ).toInt() else hoveredControlWidth
    }

    override fun getHoveredControlWidth(): Int {
        return min(textRenderer.getWidth(getValueText()).toDouble(), getUnshiftedLength().toDouble()).toInt()
    }

    override fun getValueText(): Text {
        if (!inputFieldFocused && inputField.isEmpty()) return emptyText
        return Text.literal(inputField)
    }
}