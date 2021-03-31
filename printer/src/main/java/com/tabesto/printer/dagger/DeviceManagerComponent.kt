package com.tabesto.printer.dagger

import com.tabesto.printer.multiprinter.DeviceManagerImpl
import dagger.Component

@Component(modules = [DeviceManagerModule::class])
interface DeviceManagerComponent {
    fun inject(deviceManagerImpl: DeviceManagerImpl)
}
