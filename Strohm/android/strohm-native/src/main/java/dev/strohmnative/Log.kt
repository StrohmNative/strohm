package dev.strohmnative

class Log {
    companion object {
        fun debug(vararg args: Any) {
            log(Level.DEBUG, args)
        }

        fun info(vararg args: Any) {
            log(Level.INFO, args)
        }

        fun warn(vararg args: Any) {
            log(Level.WARN, args)
        }

        fun error(vararg args: Any) {
            log(Level.ERROR, args)
        }

        private fun log(level: Level, args: Array<out Any>) {
            val argsWithLevel = arrayListOf<Any>(level.rawValue).plus(args)
            val encodedArgs = StrohmNative.getInstance().comms.encode(argsWithLevel)

            StrohmNative.getInstance().call("globalThis.strohm_native.log.log_from_native(\"$encodedArgs\")")
        }
    }

    enum class Level(val rawValue: String) {
        DEBUG("debug"),
        INFO("info"),
        WARN("warn"),
        ERROR("error")
    }
}
