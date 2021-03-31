package com.tabesto.printer.dagger

import com.tabesto.printer.multiprinter.DeviceManagerImpl
import dagger.Component

@Suppress("unused")
@Component(modules = [DeviceManagerModuleTest::class])
interface DeviceManagerComponentTest {
    fun inject(app: DeviceManagerImpl)
}
