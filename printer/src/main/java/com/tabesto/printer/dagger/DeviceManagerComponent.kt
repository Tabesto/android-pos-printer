package com.tabesto.printer.dagger

import com.tabesto.printer.multiprinter.DeviceManagerImpl
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DeviceManagerModule::class])
interface DeviceManagerComponent {
    fun inject(deviceManagerImpl: DeviceManagerImpl)
}
