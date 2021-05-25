package com.tabesto.printer.dagger

import com.tabesto.printer.multiprinter.DeviceManagerImpl
import dagger.Component
import javax.inject.Singleton

@Suppress("unused")
@Singleton
@Component(modules = [DeviceManagerModuleTest::class])
interface DeviceManagerComponentTest {
    fun inject(app: DeviceManagerImpl)
}
