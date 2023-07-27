package app.moreo.chatpimper.utils

import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import java.util.function.Consumer
import java.util.function.Supplier

inline fun YetAnotherConfigLib.Builder.category(action: ConfigCategory.Builder.() -> Unit): ConfigCategory {
    return ConfigCategory.createBuilder().apply(action).build().also {
        this.category(it)
    }
}

inline fun ConfigCategory.Builder.group(action: OptionGroup.Builder.() -> Unit): OptionGroup {
    return OptionGroup.createBuilder().apply(action).build().also {
        this.group(it)
    }
}

inline fun <reified T: Any> OptionGroup.Builder.option(action: Option.Builder<T>.() -> Unit): Option<T> {
    return Option.createBuilder<T>().apply(action).build().also {
        this.option(it)
    }
}

class BindingBuilder<T> {
    var def: T? = null
    var getter: Supplier<T & Any>? = null
    var setter: Consumer<T & Any>? = null
}

inline fun <reified T> Option.Builder<T>.binding(binding: BindingBuilder<T>.() -> Unit) {
    BindingBuilder<T>().apply(binding).let {
        if (it.def == null || it.getter == null || it.setter == null) throw NullPointerException("Binding must have all three parameters")
        this.binding(it.def!!, it.getter!!, it.setter!!)
    }
}