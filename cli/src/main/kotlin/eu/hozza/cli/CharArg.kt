package eu.hozza.cli

import kotlinx.cli.ArgType

/**
 * Argument type for char values.
 */
object CharArg : ArgType<Char>(true) {
    override val description: kotlin.String
        get() = "{ Char }"

    override fun convert(value: kotlin.String, name: kotlin.String): Char {
        check(value.length <= 1)
        if (value == "")
            return '?'
        return value.first()
    }
}
