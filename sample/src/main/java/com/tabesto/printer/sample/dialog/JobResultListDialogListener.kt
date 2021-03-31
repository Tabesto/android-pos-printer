package com.tabesto.printer.sample.dialog

import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult

interface JobResultListDialogListener {
    fun clearListOfJobsResult()
    fun onJobResultErrorSelected(jobResult: DeviceManagerJobResult)
}
