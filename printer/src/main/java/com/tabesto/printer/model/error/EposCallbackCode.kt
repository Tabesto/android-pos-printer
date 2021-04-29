package com.tabesto.printer.model.error

import com.epson.epos2.Epos2CallbackCode

@Suppress("unused")
enum class EposCallbackCode(val codeInt: Int, val codeString: String) {
    EPOS_CALLBACK_CODE_SUCCESS(Epos2CallbackCode.CODE_SUCCESS, "PRINT_SUCCESS"),
    EPOS_CALLBACK_CODE_PRINTING(Epos2CallbackCode.CODE_PRINTING, "PRINTING"),
    EPOS_CALLBACK_CODE_ERR_AUTORECOVER(Epos2CallbackCode.CODE_ERR_AUTORECOVER, "ERR_AUTORECOVER"),
    EPOS_CALLBACK_CODE_ERR_COVER_OPEN(Epos2CallbackCode.CODE_ERR_COVER_OPEN, "ERR_COVER_OPEN"),
    EPOS_CALLBACK_CODE_ERR_CUTTER(Epos2CallbackCode.CODE_ERR_CUTTER, "ERR_CUTTER"),
    EPOS_CALLBACK_CODE_ERR_MECHANICAL(Epos2CallbackCode.CODE_ERR_MECHANICAL, "ERR_MECHANICAL"),
    EPOS_CALLBACK_CODE_ERR_EMPTY(Epos2CallbackCode.CODE_ERR_EMPTY, "ERR_EMPTY"),
    EPOS_CALLBACK_CODE_ERR_UNRECOVERABLE(Epos2CallbackCode.CODE_ERR_UNRECOVERABLE, "ERR_UNRECOVERABLE"),
    EPOS_CALLBACK_CODE_ERR_FAILURE(Epos2CallbackCode.CODE_ERR_FAILURE, "ERR_FAILURE"),
    EPOS_CALLBACK_CODE_ERR_NOT_FOUND(Epos2CallbackCode.CODE_ERR_NOT_FOUND, "ERR_NOT_FOUND"),
    EPOS_CALLBACK_CODE_ERR_SYSTEM(Epos2CallbackCode.CODE_ERR_SYSTEM, "ERR_SYSTEM"),
    EPOS_CALLBACK_CODE_ERR_PORT(Epos2CallbackCode.CODE_ERR_PORT, "ERR_PORT"),
    EPOS_CALLBACK_CODE_ERR_TIMEOUT(Epos2CallbackCode.CODE_ERR_TIMEOUT, "ERR_TIMEOUT"),
    EPOS_CALLBACK_CODE_ERR_JOB_NOT_FOUND(Epos2CallbackCode.CODE_ERR_JOB_NOT_FOUND, "CODE_ERR_JOB_NOT_FOUND"),
    EPOS_CALLBACK_CODE_ERR_SPOOLER(Epos2CallbackCode.CODE_ERR_SPOOLER, "CODE_ERR_SPOOLER"),
    EPOS_CALLBACK_CODE_ERR_BATTERY_LOW(Epos2CallbackCode.CODE_ERR_BATTERY_LOW, "CODE_ERR_BATTERY_LOW"),
    EPOS_CALLBACK_CODE_ERR_TOO_MANY_REQUESTS(Epos2CallbackCode.CODE_ERR_TOO_MANY_REQUESTS, "CODE_ERR_TOO_MANY_REQUESTS"),
    EPOS_CALLBACK_CODE_ERR_REQUEST_ENTITY_TOO_LARGE(
        Epos2CallbackCode.CODE_ERR_REQUEST_ENTITY_TOO_LARGE,
        "CODE_ERR_REQUEST_ENTITY_TOO_LARGE"
    )
}