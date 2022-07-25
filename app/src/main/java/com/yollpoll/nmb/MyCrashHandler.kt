package com.yollpoll.nmb

import android.content.Context
import android.util.Log
import com.yollpoll.ilog.Ilog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Created by spq on 2022/1/14
 */
private const val MIN_STACK_OFFSET = 5
private val TOP_LEFT_CORNER: Char = '┌'
private const val BOTTOM_LEFT_CORNER = '└'
private const val MIDDLE_CORNER = '├'
private const val HORIZONTAL_LINE = '│'
private const val DOUBLE_DIVIDER = "────────────────────────────────────────────────────────"
private const val SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
private val TOP_BORDER: String =
    TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER
private val BOTTOM_BORDER: String =
    BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER
private val MIDDLE_BORDER: String =
    MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER
private const val TAG = "MyCrashHandler"


class MyCrashHandler @Inject constructor(@ApplicationContext val context: Context, val log: Ilog) :
    Thread.UncaughtExceptionHandler {
    private var defaultHandler = Thread.getDefaultUncaughtExceptionHandler()


    override fun uncaughtException(t: Thread, e: Throwable) {
        val stackMessage = getStackStraceMessage(e)
        log.saveLog("crash", stackMessage)
        defaultHandler.uncaughtException(t, e)
    }
}

fun getStackStraceMessage(e: Throwable): String {
    val trace = e.stackTrace
    var log = StringBuilder("\n")
    log.append(TOP_BORDER + "\n")
    log.append(HORIZONTAL_LINE + " Thread: " + Thread.currentThread().name + "\n")
    log.append(MIDDLE_BORDER + "\n")
    log.append(HORIZONTAL_LINE + " Message: " + e.localizedMessage + "\n")
    log.append(MIDDLE_BORDER + "\n")
    var level = ""

    for (i in trace.size downTo 0) {
        val stackIndex: Int = i
        if (stackIndex >= trace.size) {
            continue
        }
        val builder = StringBuilder()
        builder.append(HORIZONTAL_LINE)
            .append(' ')
            .append(level)
            .append(getSimpleClassName(trace[stackIndex].className))
            .append(".")
            .append(trace[stackIndex].methodName)
            .append(" ")
            .append(" (")
            .append(trace[stackIndex].fileName)
            .append(":")
            .append(trace[stackIndex].lineNumber)
            .append(")")
        level += "   "
        log.append(builder.toString() + "\n")
    }
    log.append(BOTTOM_BORDER)
    return log.toString()
}

private fun getSimpleClassName(name: String?): String {
    if (null == name)
        return ""
    val lastIndex = name.lastIndexOf(".")
    return name.substring(lastIndex + 1)
}


