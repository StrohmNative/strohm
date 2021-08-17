package dev.strohmnative.strohm

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
            val encodedArgs = Strohm.getInstance().comms.encode(argsWithLevel)

            Strohm.getInstance().call("globalThis.strohm.log.log_from_native(\"$encodedArgs\")")
        }
    }

    enum class Level(val rawValue: String) {
        DEBUG("debug"),
        INFO("info"),
        WARN("warn"),
        ERROR("error")
    }
}
