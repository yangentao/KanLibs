@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.kan.ext

import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import dev.entao.kan.appbase.*
import dev.entao.kan.base.ColorX
import dev.entao.kan.theme.ViewSize


val TextView.styleX: ButtonStyle get() = ButtonStyle(this)

fun TextView.style(block: ButtonStyle.() -> Unit) {
    val a = ButtonStyle(this)
    a.block()
    a.setup()
}

class ButtonStyle(val button: TextView) {
    var style: Int = S_FILL
        private set

    var fadeColor: Int = ColorX.fade
    var textDisabledColor: Int = TX_DISABLED
    var backDisabledColor: Int = BG_DISABLED

    var corner: Int = 4

    var fillColor: Int = Color.TRANSPARENT
    var strokeColor: Int = Color.LTGRAY
    var textColor: Int? = null

    fun cornersRound(): ButtonStyle {
        var h = button.height
        if (h <= 0) {
            h = button.layoutParams?.height ?: 0
        }
        if (h > 0) {
            return corners(px2dp(h / 2))
        }
        return corners(ViewSize.ButtonHeight / 2)
    }

    fun corners(corner: Int): ButtonStyle {
        this.corner = corner
        return this
    }

    fun fade(color: Int): ButtonStyle {
        fadeColor = color
        return this
    }

    fun fill(color: Int): ButtonStyle {
        style = S_FILL
        fillColor = color
        return this
    }

    fun outline(color: Int): ButtonStyle {
        style = S_OUTLINE
        textColor = color
        return this
    }

    fun text(color: Int): ButtonStyle {
        style = S_TEXT
        textColor = color
        return this
    }

    fun setup() {
        if (button is Button) {
            button.elevation = 4.dpf
        }
        val normalShape = ShapeRect(fillColor, corner)
        val fadeShape = ShapeRect(fadeColor, corner)
        when (style) {
            S_FILL -> {
                val disabledShape = ShapeRect(backDisabledColor, corner)
                button.backDrawableList(normalShape.value) {
                    pressed(fadeShape.value)
                    checked(fadeShape.value)
                    disabled(disabledShape.value)
                }

                if (textColor != null) {
                    button.textColor(textColor!!)
                }
            }
            S_OUTLINE -> {
                val aa = normalShape.stroke(1, strokeColor).value
                val bb = fadeShape.stroke(1, fadeColor).value
                button.background = listDrawable(aa) {
                    pressed(bb)
                    checked(bb)
                }
                val txColor = this.textColor
                if (txColor != null) {
                    button.textColorList(txColor) {
                        disabled(textDisabledColor)
                    }
                }

            }
            S_TEXT -> {
                button.background = listDrawable(normalShape.value) {
                    pressed(fadeShape.value)
                    checked(fadeShape.value)
                }
                val txColor = this.textColor
                if (txColor != null) {
                    button.textColorList(txColor) {
                        disabled(textDisabledColor)
                    }
                }
            }
            else -> {
                return
            }
        }


    }

    fun outlineBlue(): ButtonStyle {
        outline(C_BLUE)
        return this
    }

    fun outlineRed(): ButtonStyle {
        outline(C_RED)
        return this
    }

    fun outlineGreen(): ButtonStyle {
        outline(C_GREEN)
        return this
    }

    fun outlineTheme(): ButtonStyle {
        outline(ColorX.theme)
        return this
    }

    fun textBlue(): ButtonStyle {
        text(C_BLUE)
        return this
    }

    fun textRed(): ButtonStyle {
        text(C_RED)
        return this
    }

    fun textGreen(): ButtonStyle {
        text(C_GREEN)
        return this
    }

    fun textTheme(): ButtonStyle {
        text(ColorX.theme)
        return this
    }

    fun fillBlue(): ButtonStyle {
        this.fill(C_BLUE)
        textColor = Color.WHITE
        return this
    }

    fun fillRed(): ButtonStyle {
        this.fill(C_RED)
        textColor = Color.WHITE
        return this
    }

    fun fillGreen(): ButtonStyle {
        this.fill(C_GREEN)
        textColor = Color.WHITE
        return this
    }

    fun fillTheme(): ButtonStyle {
        this.fill(ColorX.theme)
        textColor = Color.WHITE
        return this
    }

    companion object {
        const val S_FILL = 0
        const val S_OUTLINE = 1
        const val S_TEXT = 2

        var C_BLUE: Int = ColorX.actionBlue
        var C_RED: Int = ColorX.actionRed
        var C_GREEN: Int = ColorX.actionGreen
        var TX_DISABLED: Int = ColorX.textDisabled
        var BG_DISABLED: Int = ColorX.backDisabled
    }
}
