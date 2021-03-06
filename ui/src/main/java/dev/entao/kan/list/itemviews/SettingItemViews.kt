@file:Suppress("unused")

package dev.entao.kan.list.itemviews

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Size
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import dev.entao.kan.appbase.dp
import dev.entao.kan.appbase.sized
import dev.entao.kan.base.FormOptions
import dev.entao.kan.base.Prop
import dev.entao.kan.creator.add
import dev.entao.kan.creator.imageView
import dev.entao.kan.creator.textView
import dev.entao.kan.dialogs.DialogX
import dev.entao.kan.ext.*
import dev.entao.kan.base.ImageDef
import dev.entao.kan.appbase.resDrawable
import dev.entao.kan.theme.Space
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 16/3/13.
 */


open class HorItemView(context: Context) : LinearLayout(context) {
    var positionBind: Int = 0
    var argInt: Int = 0
    var argString: String = ""

    init {
        needId()
        horizontal()
        gravityCenterVertical()
        padding(Space.X, 5, Space.X, 5)
        backColorWhiteFade()
        this.minimumHeight = 50.dp
        this.layoutParams = MParam.FillW.WrapH
    }

}

open class LabelItemView(context: Context) : HorItemView(context) {
    val labelView: TextView = textView(LParam.FlexX.WrapH.LeftCenter) {
        textSizeB().textColorMajor().singleLine()
    }

    init {
        backColorWhite()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        backColorWhiteFade()
    }

    var label: String
        get() = this.labelView.textS
        set(value) {
            this.labelView.text = value
        }
}

fun <T : LabelItemView> T.leftImage(d: Drawable): T {
    labelView.setCompoundDrawables(d, null, null, null)
    labelView.compoundDrawablePadding = 10.dp
    return this
}

open class LabelValueItemView(context: Context) : LabelItemView(context) {
    val valueView: TextView = textView(LParam.Wrap.RightCenter) {
        textSizeC().textColorMinor().gravityRightCenter().singleLine()
    }
}

fun <T : LabelValueItemView> T.rightImage(d: Drawable): T {
    valueView.setCompoundDrawables(null, null, d, null)
    valueView.compoundDrawablePadding = 10.dp
    return this
}

fun <T : LabelValueItemView> T.more(): T {
    this.padRight(2)
    val d = ImageDef.more.resDrawable.sized(12)
    valueView.setCompoundDrawables(null, null, d, null)
    valueView.compoundDrawablePadding = 2.dp
    return this
}

class LabelTextItemView(context: Context) : LabelValueItemView(context) {

    var _onValueChanged: (String) -> Unit = {}
    var configEditBlock: (EditText) -> Unit = {}

    init {
        valueView.multiLine().maxLines(2)
    }

    fun onValueChanged(block: (String) -> Unit) {
        autoInput()
        _onValueChanged = block
    }

    fun autoInput() {
        this.click(::showInput)
    }

    private fun showInput() {
        val oldText = valueView.textS
        DialogX(context).showInput(label, oldText, configEditBlock) { s ->
            if (s != oldText) {
                _onValueChanged(s)
            }
        }
    }

    var value: String?
        get() = this.valueView.text?.toString()
        set(value) {
            this.valueView.text = value ?: ""
        }
}


open class LabelListItemView<T : Any>(context: Context) : LabelValueItemView(context) {
    private var _value: T? = null
    val options: ArrayList<Pair<T, String>> = ArrayList()

    var onValueChanged: (T) -> Unit = {}

    init {
        valueView.text = "请选择"
        this.click(::clickMe)
    }

    private fun clickMe() {
        DialogX(context).showListItem(options, null, { it.second }) { selVal ->
            val changed = selVal.first != _value
            value = selVal.first
            if (changed) {
                onValueChanged(selVal.first)
            }
        }
    }

    var value: T?
        get() = _value
        set(value) {
            _value = value
            valueView.text = options.firstOrNull { it.first == value }?.second ?: value?.toString() ?: ""
        }


}

class IntLabelListItemView(context: Context) : LabelListItemView<Int>(context) {


    fun optionsFrom(p: Prop) {
        val ops = p.findAnnotation<FormOptions>()?.options ?: return
        for (s in ops) {
            val ls = s.split(':')
            if (ls.size == 2) {
                val key = ls[0].toIntOrNull() ?: return
                options += key to ls[1]
            } else if (ls.size == 1) {
                val key = ls[0].toIntOrNull() ?: return
                options += key to key.toString()
            }
        }
    }


}

class StringLabelListItemView(context: Context) : LabelListItemView<String>(context) {

    fun optionsFrom(p: Prop) {
        val ops = p.findAnnotation<FormOptions>()?.options ?: return
        for (s in ops) {
            val ls = s.split(':')
            if (ls.size == 2) {
                options += ls[0] to ls[1]
            } else if (ls.size == 1) {
                options += ls[0] to ls[0]
            }
        }
    }
}

class LabelSwitchItemView(context: Context) : LabelItemView(context) {
    private val switchView: SwitchButton = this.add(SwitchButton(context), LParam.width(SwitchButton.WIDTH).height(SwitchButton.HEIGHT).RightCenter)
    private var _value: Boolean = false
    var onValueChanged: (Boolean) -> Unit = {}

    init {
        switchView.setOnCheckedChangeListener { _, isChecked ->
            if (_value != isChecked) {
                _value = isChecked
                onValueChanged(isChecked)
            }
        }

    }

    var value: Boolean
        get() = _value
        set(value) {
            _value = value
            this.switchView.isChecked = value
        }

}

class LabelImageItemView(context: Context) : LabelItemView(context) {
    val imageView: ImageView
    private var size: Size = Size(80, 80)

    init {
        imageView = this.imageView(LParam.size(size.width, size.height).RightCenter) {
            scaleCenterCrop()
        }
        minimumHeight = size.height.dp
    }

    var image: Drawable?
        get() = this.imageView.drawable
        set(value) {
            this.imageView.setImageDrawable(value)
        }

    var imageSize: Size
        get() = this.size
        set(value) {
            this.size = value
            val lp = imageView.layoutParams as LinearLayout.LayoutParams
            lp.size(size.width, size.height)
            this.minimumHeight = size.height.dp
        }


}


fun ViewGroup.labelImage(param: ViewGroup.LayoutParams, block: LabelImageItemView.() -> Unit): LabelImageItemView {
    val v = LabelImageItemView(this.context)
    this.addView(v, param)
    v.block()
    return v
}

fun ViewGroup.labelImage(block: LabelImageItemView.() -> Unit): LabelImageItemView {
    val v = LabelImageItemView(this.context)
    this.addView(v)
    v.block()
    return v
}

fun ViewGroup.labelText(param: ViewGroup.LayoutParams, block: LabelTextItemView.() -> Unit): LabelTextItemView {
    val v = LabelTextItemView(this.context)
    this.addView(v, param)
    v.block()
    return v
}

fun ViewGroup.labelText(block: LabelTextItemView.() -> Unit): LabelTextItemView {
    val v = LabelTextItemView(this.context)
    this.addView(v)
    v.block()
    return v
}

fun ViewGroup.labelListString(param: ViewGroup.LayoutParams, block: StringLabelListItemView.() -> Unit): StringLabelListItemView {
    val v = StringLabelListItemView(this.context)
    this.addView(v, param)
    v.block()
    return v
}

fun ViewGroup.labelListString(block: StringLabelListItemView.() -> Unit): StringLabelListItemView {
    val v = StringLabelListItemView(this.context)
    this.addView(v)
    v.block()
    return v
}

fun ViewGroup.labelListInt(param: ViewGroup.LayoutParams, block: IntLabelListItemView.() -> Unit): IntLabelListItemView {
    val v = IntLabelListItemView(this.context)
    this.addView(v, param)
    v.block()
    return v
}

fun ViewGroup.labelListInt(block: IntLabelListItemView.() -> Unit): IntLabelListItemView {
    val v = IntLabelListItemView(this.context)
    this.addView(v)
    v.block()
    return v
}

fun ViewGroup.labelSwitch(param: ViewGroup.LayoutParams, block: LabelSwitchItemView.() -> Unit): LabelSwitchItemView {
    val v = LabelSwitchItemView(this.context)
    this.addView(v, param)
    v.block()
    return v
}

fun ViewGroup.labelSwitch(block: LabelSwitchItemView.() -> Unit): LabelSwitchItemView {
    val v = LabelSwitchItemView(this.context)
    this.addView(v)
    v.block()
    return v
}