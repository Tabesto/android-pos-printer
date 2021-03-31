package com.tabesto.printer.utils

import android.annotation.SuppressLint
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

/**
 * AppExceptionHandler implement interface UncaughtExceptionHandler
 * it defines the behaviour of the program when an unhandled exception produce
 * we store the stacktrace
 *
 */
@SuppressLint("LogNotTimber")
class AppExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(p0: Thread, p1: Throwable) {
        Log.e("in UNCAUGHT !", " throwable message " + p1.message)

        val stackTrace = StringWriter()
        p1.printStackTrace(PrintWriter(stackTrace))
        val errorReport = StringBuilder()
        errorReport.append("************ CAUSE OF ERROR ************\n\n")
        errorReport.append(stackTrace.toString())
    }
}
