package com.tabesto.printer.sample.adapter

import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult

interface JobResultListAdapterListener {
    fun onClickOnJobResult(jobResult: DeviceManagerJobResult)
}
