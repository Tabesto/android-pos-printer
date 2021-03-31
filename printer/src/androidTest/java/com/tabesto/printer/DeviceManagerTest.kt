package com.tabesto.printer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.epson.epos2.printer.PrinterStatusInfo
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.tabesto.printer.dagger.DaggerDeviceManagerComponentTest
import com.tabesto.printer.dagger.DeviceManagerModuleTest
import com.tabesto.printer.model.ConnectionMode
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterManaged
import com.tabesto.printer.model.PrinterModel
import com.tabesto.printer.model.PrinterRegion
import com.tabesto.printer.model.PrinterType
import com.tabesto.printer.model.ScopeTag.PRINT_DATA
import com.tabesto.printer.model.ScopeTag.DISCONNECT
import com.tabesto.printer.model.ScopeTag.CONNECT
import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult
import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.model.status.ConnectionStatus.CONNECTED
import com.tabesto.printer.model.status.ConnectionStatus.DISCONNECTED
import com.tabesto.printer.model.status.PrinterStatus
import com.tabesto.printer.model.ticket.FeedLine
import com.tabesto.printer.model.ticket.StringLine
import com.tabesto.printer.model.ticket.StyleLine
import com.tabesto.printer.model.ticket.TicketData
import com.tabesto.printer.multiprinter.DeviceManagerConnectListener
import com.tabesto.printer.multiprinter.DeviceManagerImpl
import com.tabesto.printer.multiprinter.DeviceManagerListener
import com.tabesto.printer.multiprinter.DeviceManagerPrintListener
import com.tabesto.printer.multiprinter.DeviceManagerStatusListener
import com.tabesto.printer.utils.Constants.MAIN_JOB_IS_RUNNING
import com.tabesto.printer.utils.Constants.PRINTER_DATA_NOT_MANAGED
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceManagerTest {
    private val context: Context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var mockDeviceManagerListener: DeviceManagerListener
    private lateinit var mockDeviceManagerConnectListener: DeviceManagerConnectListener
    private lateinit var mockDeviceManagerPrintListener: DeviceManagerPrintListener
    private lateinit var mockDeviceManagerStatusListener: DeviceManagerStatusListener
    private lateinit var mockPrinter: Printer
    private lateinit var mockPrinterSecond: Printer
    private lateinit var mockPrinterThird: Printer
    private lateinit var mockPrinterException: PrinterException
    private lateinit var listOfPrinters: HashMap<PrinterData, Printer>
    private lateinit var listOfTwoPrinterData: ArrayList<PrinterData>
    private lateinit var listOfThreePrinterData: ArrayList<PrinterData>
    private lateinit var deviceManager: DeviceManagerImpl
    private lateinit var printerStatusConnected: PrinterStatus
    private lateinit var captorListOfJobResult: KArgumentCaptor<ArrayList<DeviceManagerJobResult>>

    private val printerData: PrinterData = PrinterData(
        PrinterModel.PRINTER_TM_M10,
        PrinterRegion.PRINTER_ANK,
        PRINTER_ADDRESS,
        ConnectionMode.AUTO,
        PrinterType.PRINTER_EPSON
    )
    private val printerDataSecond: PrinterData = PrinterData(
        PrinterModel.PRINTER_TM_M30,
        PrinterRegion.PRINTER_ANK,
        PRINTER_ADDRESS_SECOND,
        ConnectionMode.MANUAL,
        PrinterType.PRINTER_EPSON
    )
    private val printerDataThird: PrinterData = PrinterData(
        PrinterModel.PRINTER_TM_T20III,
        PrinterRegion.PRINTER_ANK,
        PRINTER_ADDRESS_THIRD,
        ConnectionMode.MANUAL,
        PrinterType.PRINTER_EPSON
    )
    private val printerDataUnknown: PrinterData = PrinterData(
        PrinterModel.PRINTER_TM_T20III,
        PrinterRegion.PRINTER_ANK,
        PRINTER_ADDRESS_UNKNOWN,
        ConnectionMode.MANUAL,
        PrinterType.PRINTER_EPSON
    )

    companion object {
        private const val PRINTER_ADDRESS = "BT:00:01:90:BE:3D:69"
        private const val PRINTER_ADDRESS_SECOND = "TCP:00:01:90:BE:3D:F2"
        private const val PRINTER_ADDRESS_THIRD = "BT:33:01:90:BE:3D:33"
        private const val PRINTER_ADDRESS_UNKNOWN = "BT:33:01:90:66:66:66"
        private val ticketDataBuilder = TicketData.TicketDataBuilder()
        private val ticketData: TicketData = ticketDataBuilder
            .withFeedLine(FeedLine())
            .withLine(StringLine("THE STORE 123 (555) 555 – 5555", StyleLine()))
            .withFeedLine(FeedLine()).build()
    }

    @Before
    fun setUp() {
        // given
        listOfTwoPrinterData = ArrayList()
        listOfThreePrinterData = ArrayList()
        listOfPrinters = HashMap()
        captorListOfJobResult = argumentCaptor()


        mockDeviceManagerListener = mock()
        mockDeviceManagerConnectListener = mock()
        mockDeviceManagerPrintListener = mock()
        mockDeviceManagerStatusListener = mock()
        mockPrinter = mock()
        mockPrinterSecond = mock()
        mockPrinterThird = mock()
        mockPrinterException = mock()

        listOfTwoPrinterData.add(printerData)
        listOfTwoPrinterData.add(printerDataSecond)

        listOfThreePrinterData.add(printerData)
        listOfThreePrinterData.add(printerDataSecond)
        listOfThreePrinterData.add(printerDataThird)

        printerStatusConnected = PrinterStatus(connectionStatus = CONNECTED)

        deviceManager = DeviceManagerImpl.getInstance()
        deviceManager.setListeners(
            deviceManagerInitListener = null,
            deviceManagerConnectListener = mockDeviceManagerConnectListener,
            deviceManagerPrintListener = mockDeviceManagerPrintListener,
            deviceManagerDiscoveryListener = null,
            deviceManagerStatusListener = mockDeviceManagerStatusListener,
            deviceManagerListener = mockDeviceManagerListener
        )
        DaggerDeviceManagerComponentTest.builder().deviceManagerModuleTest(DeviceManagerModuleTest(listOfPrinters)).build()
            .inject(deviceManager)

        whenever(mockPrinter.printerData).doReturn(printerData)
        whenever(mockPrinterSecond.printerData).doReturn(printerDataSecond)
        whenever(mockPrinterThird.printerData).doReturn(printerDataThird)
    }

    //region INITIALIZE_AND_REMOVE
    @Test
    fun test_initializePrinter_successful() {
        // when
        val result: Unit = deviceManager.initializePrinter(printerData, context)

        // then
        assertThat(result, `is`(equalTo(Unit)))
        verify(mockDeviceManagerListener, never()).onDeviceManagerErrorForAJobResult(any(), any())
        assertThat(deviceManager.getManagedPrinterDataList().size, `is`(equalTo(1)))
    }

    @Test
    fun test_initialize_two_printers_successful() {
        // when
        val result: Unit = deviceManager.initializePrinter(printerData, context)
        val resultSecond: Unit = deviceManager.initializePrinter(printerDataSecond, context)

        // then
        assertThat(result, `is`(equalTo(Unit)))
        assertThat(resultSecond, `is`(equalTo(Unit)))
        verify(mockDeviceManagerListener, never()).onDeviceManagerErrorForAJobResult(any(), any())
        assertThat(deviceManager.getManagedPrinterDataList().size, `is`(equalTo(2)))
    }

    @Test
    fun test_initialize_printer_failure() {
        // when
        deviceManager.initializePrinter(printerData, context)
        deviceManager.initializePrinter(printerData, context)

        // then
        verify(mockDeviceManagerListener, never()).onDeviceManagerErrorForAJobResult(any(), any())
        assertThat(deviceManager.getManagedPrinterDataList().size, `is`(equalTo(1)))
    }

    @Test
    fun test_initialize_list_of_printer_data_successful() {
        // when
        val result: Unit = deviceManager.initializePrinter(listOfTwoPrinterData, context)

        // then
        assertThat(result, `is`(equalTo(Unit)))
        verify(mockDeviceManagerListener, never()).onDeviceManagerErrorForAJobResult(any(), any())
        assertThat(deviceManager.getManagedPrinterDataList().size, `is`(equalTo(2)))
    }

    @Test
    fun test_initialize_list_of_two__printer_data_failure() {
        // given
        val listOfPrinterData: ArrayList<PrinterData> = ArrayList()
        listOfPrinterData.add(printerData)
        listOfPrinterData.add(printerData)

        // when
        val result: Unit = deviceManager.initializePrinter(listOfPrinterData, context)

        // then
        assertThat(result, `is`(equalTo(Unit)))
        assertThat(deviceManager.getManagedPrinterDataList().size, `is`(equalTo(1)))
        verify(mockDeviceManagerListener, never()).onDeviceManagerErrorForAJobResult(any(), any())
    }

    @Test
    fun test_removePrinter_successful() {
        // given
        whenever(mockPrinter.getStatusRaw()).doAnswer { printerStatusConnected }
        listOfPrinters[printerData] = mockPrinter

        // when
        val result = deviceManager.removePrinter(printerData)

        // then
        assertThat(result, `is`(false))
    }

    @Test
    fun test_removePrinter_failure() {
        // given
        val printerStatusDisconnected = PrinterStatus(connectionStatus = DISCONNECTED)
        whenever(mockPrinter.getStatusRaw()).doAnswer { printerStatusDisconnected }
        listOfPrinters[printerData] = mockPrinter

        // when
        val result = deviceManager.removePrinter(printerData)

        // then
        assertThat(result, `is`(true))
    }
    //endregion INITIALIZE_AND_REMOVE

    //region CONNECT
    @Test
    fun test_connect_printer_successful() {
        // given
        whenever(mockPrinter.connectPrinter()).doAnswer {
            deviceManager.onConnectSuccess(printerData)
        }
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.connectPrinter(printerData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onConnectResult(any())
    }

    @Test
    fun test_connect_printer_failure() {
        // given
        whenever(mockPrinter.connectPrinter()).doAnswer { deviceManager.onConnectFailure(printerData, mockPrinterException) }
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.connectPrinter(printerData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onConnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(1)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(false)))
    }

    @Test
    fun test_connect_list_of_two_printerData_successful() {
        // given
        whenever(mockPrinter.connectPrinter()).doAnswer {
            deviceManager.onConnectSuccess(printerData)
        }
        // INFO : Line below simulate an asynchronous return of onConnectSuccess from the two printers at the same time
        // This behaviour gives us the possibility to check if we manage correctly le list of jobs results
        whenever(mockPrinterSecond.connectPrinter()).doAnswer {
            deviceManager.onConnectSuccess(printerDataSecond)
        }
        listOfPrinters[printerData] = mockPrinter
        listOfPrinters[printerDataSecond] = mockPrinterSecond

        // when
        deviceManager.connectPrinter(listOfTwoPrinterData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onConnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(2)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.firstValue[1].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[1].printerData, `is`(equalTo(printerDataSecond)))
    }

    @Test
    fun test_connect_list_of_two_printerData__with_one_successful_and_one_failure() {
        // given
        whenever(mockPrinter.connectPrinter()).doAnswer {
            deviceManager.onConnectFailure(printerData, mockPrinterException)
        }
        whenever(mockPrinterSecond.connectPrinter()).doAnswer {
            deviceManager.onConnectSuccess(printerDataSecond)
        }
        listOfPrinters[printerData] = mockPrinter
        listOfPrinters[printerDataSecond] = mockPrinterSecond

        // when
        deviceManager.connectPrinter(listOfTwoPrinterData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onConnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(2)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(CONNECT)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.firstValue[1].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[1].scopeTag, `is`(equalTo(CONNECT)))
        assertThat(captorListOfJobResult.firstValue[1].printerData, `is`(equalTo(printerDataSecond)))
    }

    @Test
    fun test_connect_list_of_two_printerData_with_two_failure() {
        // given
        whenever(mockPrinter.connectPrinter()).doAnswer {
            deviceManager.onConnectFailure(printerData, mockPrinterException)
        }
        whenever(mockPrinterSecond.connectPrinter()).doAnswer {
            deviceManager.onConnectFailure(printerDataSecond, mockPrinterException)
        }
        listOfPrinters[printerData] = mockPrinter
        listOfPrinters[printerDataSecond] = mockPrinterSecond

        // when
        deviceManager.connectPrinter(listOfTwoPrinterData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onConnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(2)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(CONNECT)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.firstValue[1].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.firstValue[1].scopeTag, `is`(equalTo(CONNECT)))
        assertThat(captorListOfJobResult.firstValue[1].printerData, `is`(equalTo(printerDataSecond)))
    }

    @Test
    fun test_connect_list_of_three_printerData_with_one_success_two_failure() {
        // given
        whenever(mockPrinter.connectPrinter()).doAnswer {
            deviceManager.onConnectSuccess(printerData)
        }
        whenever(mockPrinterSecond.connectPrinter()).doAnswer {
            deviceManager.onConnectFailure(printerDataSecond, mockPrinterException)
        }
        whenever(mockPrinterThird.connectPrinter()).doAnswer {
            deviceManager.onConnectSuccess(printerDataThird)
        }
        listOfPrinters[printerData] = mockPrinter
        listOfPrinters[printerDataSecond] = mockPrinterSecond
        listOfPrinters[printerDataThird] = mockPrinterThird

        // when
        deviceManager.connectPrinter(listOfThreePrinterData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onConnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(3)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(CONNECT)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.firstValue[1].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.firstValue[1].scopeTag, `is`(equalTo(CONNECT)))
        assertThat(captorListOfJobResult.firstValue[1].printerData, `is`(equalTo(printerDataSecond)))
        assertThat(captorListOfJobResult.firstValue[2].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[2].scopeTag, `is`(equalTo(CONNECT)))
        assertThat(captorListOfJobResult.firstValue[2].printerData, `is`(equalTo(printerDataThird)))
    }

    @Test
    fun test_connect_list_of_printerData_by_passing_param_null_and_all_connect_success() {
        // given
        whenever(mockPrinter.connectPrinter()).doAnswer {
            deviceManager.onConnectSuccess(printerData)
        }
        whenever(mockPrinterSecond.connectPrinter()).doAnswer {
            deviceManager.onConnectSuccess(printerDataSecond)
        }
        listOfPrinters[printerData] = mockPrinter
        listOfPrinters[printerDataSecond] = mockPrinterSecond

        // when
        deviceManager.connectPrinter()

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onConnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(2)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(CONNECT)))
    }
    //endregion CONNECT

    //region PRINT
    @Test
    fun test_print_one_printer_successful() {
        // given
        whenever(mockPrinter.printData(ticketData)).doAnswer { deviceManager.onPrintSuccess(printerData, printerStatusConnected) }
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.printData(printerData, ticketData)

        // then
        verify(mockDeviceManagerPrintListener, times(1)).onPrintResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(1)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(PRINT_DATA)))
    }

    @Test
    fun test_print_one_printer_failure() {
        // given
        whenever(mockPrinter.printData(ticketData)).doAnswer { deviceManager.onPrintDataFailure(printerData, mockPrinterException) }
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.printData(printerData, ticketData)

        // then
        verify(mockDeviceManagerPrintListener, times(1)).onPrintResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(1)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(PRINT_DATA)))
    }

    @Test
    fun test_print_list_of_three_printerData_with_all_success() {
        // given
        whenever(mockPrinter.printData(ticketData)).doAnswer {
            deviceManager.onPrintSuccess(printerData, printerStatusConnected)
        }
        whenever(mockPrinterSecond.printData(ticketData)).doAnswer {
            deviceManager.onPrintSuccess(printerDataSecond, printerStatusConnected)
        }
        whenever(mockPrinterThird.printData(ticketData)).doAnswer {
            deviceManager.onPrintSuccess(printerDataThird, printerStatusConnected)
        }
        listOfPrinters[printerData] = mockPrinter
        listOfPrinters[printerDataSecond] = mockPrinterSecond
        listOfPrinters[printerDataThird] = mockPrinterThird

        // when
        deviceManager.printData(listOfThreePrinterData, ticketData)

        // then
        verify(mockDeviceManagerPrintListener, times(3)).onPrintResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(1)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(PRINT_DATA)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.secondValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.secondValue[0].scopeTag, `is`(equalTo(PRINT_DATA)))
        assertThat(captorListOfJobResult.secondValue[0].printerData, `is`(equalTo(printerDataSecond)))
        assertThat(captorListOfJobResult.thirdValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.thirdValue[0].scopeTag, `is`(equalTo(PRINT_DATA)))
        assertThat(captorListOfJobResult.thirdValue[0].printerData, `is`(equalTo(printerDataThird)))
    }

    @Test
    fun test_print_list_of_three_printerData_with_one_success_two_failure_and_one_deviceManagerException() {
        // given
        val listOfPrinterData: ArrayList<PrinterData> = ArrayList()
        listOfPrinterData.add(printerData)
        listOfPrinterData.add(printerDataSecond)
        listOfPrinterData.add(printerDataThird)
        listOfPrinterData.add(printerDataUnknown)
        whenever(mockPrinter.printData(ticketData)).doAnswer {
            deviceManager.onPrintDataFailure(printerData, mockPrinterException)
        }
        whenever(mockPrinterSecond.printData(ticketData)).doAnswer {
            deviceManager.onPrintSuccess(printerDataSecond, printerStatusConnected)
        }
        whenever(mockPrinterThird.printData(ticketData)).doAnswer {
            deviceManager.onPrintDataFailure(printerDataThird, mockPrinterException)
        }
        listOfPrinters[printerData] = mockPrinter
        listOfPrinters[printerDataSecond] = mockPrinterSecond
        listOfPrinters[printerDataThird] = mockPrinterThird

        // when
        deviceManager.printData(listOfPrinterData, ticketData)

        // then
        val captorJobResult = argumentCaptor<DeviceManagerJobResult>()

        verify(mockDeviceManagerPrintListener, times(3)).onPrintResult(captorListOfJobResult.capture())
        verify(mockDeviceManagerListener, times(1)).onDeviceManagerErrorForAJobResult(captorJobResult.capture(), any())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(1)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.secondValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.secondValue[0].printerData, `is`(equalTo(printerDataSecond)))
        assertThat(captorListOfJobResult.thirdValue[0].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.thirdValue[0].printerData, `is`(equalTo(printerDataThird)))
        assertThat(captorJobResult.firstValue.isSuccessful, `is`(equalTo(false)))
        assertThat(captorJobResult.firstValue.scopeTag, `is`(equalTo(PRINT_DATA)))
        assertThat(captorJobResult.firstValue.deviceManagerException?.error, `is`(equalTo(PRINTER_DATA_NOT_MANAGED)))
    }

    @Test
    fun test_print_list_of__printerData_with_param_null_and_all_print_success() {
        // given
        whenever(mockPrinter.printData(ticketData)).doAnswer {
            deviceManager.onPrintSuccess(printerData, printerStatusConnected)
        }
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.printData(ticketData = ticketData)

        // then
        verify(mockDeviceManagerPrintListener, times(1)).onPrintResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(1)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
    }
    //endregion PRINT

    //region DISCONNECT
    @Test
    fun test_disconnect_printer_successful() {
        // given
        whenever(mockPrinter.disconnectPrinter()).doAnswer { deviceManager.onDisconnectSuccess(printerData) }
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.disconnectPrinter(printerData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onDisconnectResult(any())
    }

    @Test
    fun test_disconnect_printer_failure() {
        // given
        whenever(mockPrinter.disconnectPrinter()).doAnswer { deviceManager.onDisconnectFailure(printerData, mockPrinterException) }
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.disconnectPrinter(printerData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onDisconnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(1)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(DISCONNECT)))
    }

    @Test
    fun test_disconnect_list_of_two_printerData_successful() {
        // given
        whenever(mockPrinter.disconnectPrinter()).doAnswer {
            deviceManager.onDisconnectSuccess(printerData)
        }
        // INFO : Line below simulate an asynchronous return of onDisconnectSuccess from the two printers at the same time
        // This behaviour gives us the possibility to check if we manage correctly le list of jobs results
        whenever(mockPrinterSecond.disconnectPrinter()).doAnswer {
            deviceManager.onDisconnectSuccess(printerDataSecond)
        }
        listOfPrinters[printerData] = mockPrinter
        listOfPrinters[printerDataSecond] = mockPrinterSecond

        // when
        deviceManager.disconnectPrinter(listOfTwoPrinterData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onDisconnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(2)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(DISCONNECT)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.firstValue[1].scopeTag, `is`(equalTo(DISCONNECT)))
        assertThat(captorListOfJobResult.firstValue[1].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[1].printerData, `is`(equalTo(printerDataSecond)))
    }

    @Test
    fun test_disconnect_list_of_two_printerData_with_two_failure() {
        // given
        whenever(mockPrinter.disconnectPrinter()).doAnswer {
            deviceManager.onDisconnectFailure(printerData, mockPrinterException)
        }
        whenever(mockPrinterSecond.disconnectPrinter()).doAnswer {
            deviceManager.onDisconnectFailure(printerDataSecond, mockPrinterException)
        }
        listOfPrinters[printerData] = mockPrinter
        listOfPrinters[printerDataSecond] = mockPrinterSecond

        // when
        deviceManager.disconnectPrinter(listOfTwoPrinterData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onDisconnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(2)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(DISCONNECT)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.firstValue[1].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.firstValue[1].scopeTag, `is`(equalTo(DISCONNECT)))
        assertThat(captorListOfJobResult.firstValue[1].printerData, `is`(equalTo(printerDataSecond)))
    }

    @Test
    fun test_disconnect_list_of_three_printerData_with_one_success_two_failure() {
        // given
        whenever(mockPrinter.disconnectPrinter()).doAnswer {
            deviceManager.onDisconnectSuccess(printerData)
        }
        whenever(mockPrinterSecond.disconnectPrinter()).doAnswer {
            deviceManager.onDisconnectFailure(printerDataSecond, mockPrinterException)
        }
        whenever(mockPrinterThird.disconnectPrinter()).doAnswer {
            deviceManager.onDisconnectSuccess(printerDataThird)
        }
        listOfPrinters[printerData] = mockPrinter
        listOfPrinters[printerDataSecond] = mockPrinterSecond
        listOfPrinters[printerDataThird] = mockPrinterThird

        // when
        deviceManager.disconnectPrinter(listOfThreePrinterData)

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onDisconnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(3)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(DISCONNECT)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.firstValue[1].isSuccessful, `is`(equalTo(false)))
        assertThat(captorListOfJobResult.firstValue[1].scopeTag, `is`(equalTo(DISCONNECT)))
        assertThat(captorListOfJobResult.firstValue[1].printerData, `is`(equalTo(printerDataSecond)))
        assertThat(captorListOfJobResult.firstValue[2].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[2].scopeTag, `is`(equalTo(DISCONNECT)))
        assertThat(captorListOfJobResult.firstValue[2].printerData, `is`(equalTo(printerDataThird)))
    }

    @Test
    fun test_disconnect_list_of_printerData_by_passing_param_null_and_all_connect_success() {
        // given
        whenever(mockPrinter.disconnectPrinter()).doAnswer {
            deviceManager.onDisconnectSuccess(printerData)
        }
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.disconnectPrinter()

        // then
        verify(mockDeviceManagerConnectListener, times(1)).onDisconnectResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(1)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[0].scopeTag, `is`(equalTo(DISCONNECT)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
    }
    //endregion DISCONNECT

    //region MAIN_JOB_RUNNING
    @Test
    fun test_multiple_connect_main_job_is_running() {
        // given
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.connectPrinter()
        deviceManager.connectPrinter()
        deviceManager.connectPrinter()
        deviceManager.connectPrinter()

        // then
        val captorJobResult = argumentCaptor<DeviceManagerJobResult>()
        val captorIsLastReturn = argumentCaptor<Boolean>()

        verify(mockDeviceManagerListener, times(3)).onDeviceManagerErrorForAJobResult(
            captorJobResult.capture(),
            captorIsLastReturn.capture()
        )

        assertThat(captorJobResult.firstValue.isSuccessful, `is`(equalTo(false)))
        assertThat(captorJobResult.firstValue.scopeTag, `is`(equalTo(CONNECT)))
        assertThat(captorJobResult.firstValue.deviceManagerException?.error, `is`(equalTo(MAIN_JOB_IS_RUNNING)))
        assertThat(captorIsLastReturn.firstValue, `is`(equalTo(true)))
        assertThat(captorIsLastReturn.lastValue, `is`(equalTo(true)))

        //after
        deviceManager.mainJobIsRunning = false
    }

    @Test
    fun test_multiple_disconnect_main_job_is_running() {
        // given
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.disconnectPrinter()
        deviceManager.disconnectPrinter()
        deviceManager.disconnectPrinter()
        deviceManager.disconnectPrinter()

        // then
        val captorJobResult = argumentCaptor<DeviceManagerJobResult>()
        val captorIsLastReturn = argumentCaptor<Boolean>()

        verify(mockDeviceManagerListener, times(3)).onDeviceManagerErrorForAJobResult(
            captorJobResult.capture(),
            captorIsLastReturn.capture()
        )

        assertThat(captorJobResult.firstValue.isSuccessful, `is`(equalTo(false)))
        assertThat(captorJobResult.firstValue.scopeTag, `is`(equalTo(DISCONNECT)))
        assertThat(captorJobResult.firstValue.deviceManagerException?.error, `is`(equalTo(MAIN_JOB_IS_RUNNING)))
        assertThat(captorIsLastReturn.firstValue, `is`(equalTo(true)))
        assertThat(captorIsLastReturn.lastValue, `is`(equalTo(true)))

        //after
        deviceManager.mainJobIsRunning = false
    }

    @Test
    fun test_multiple_print_in_cascade() {
        // given
        whenever(mockPrinter.printData(ticketData)).doAnswer {
            deviceManager.onPrintSuccess(
                printerData,
                PrinterStatus(connectionStatus = CONNECTED)
            )
        }
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.printData(ticketData)
        deviceManager.printData(ticketData)
        deviceManager.printData(ticketData)
        deviceManager.printData(ticketData)

        // then
        verify(mockDeviceManagerPrintListener, times(4)).onPrintResult(captorListOfJobResult.capture())
        assertThat(captorListOfJobResult.firstValue.size, `is`(equalTo(1)))
        assertThat(captorListOfJobResult.firstValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.firstValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.secondValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.secondValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.thirdValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.thirdValue[0].printerData, `is`(equalTo(printerData)))
        assertThat(captorListOfJobResult.lastValue[0].isSuccessful, `is`(equalTo(true)))
        assertThat(captorListOfJobResult.lastValue[0].printerData, `is`(equalTo(printerData)))
    }

    @Test
    fun test_multiple_connect_list_printer_data_main_job_is_running() {
        // given
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.connectPrinter()
        deviceManager.connectPrinter(listOfThreePrinterData)

        // then
        val captorJobResult = argumentCaptor<DeviceManagerJobResult>()
        val captorIsLastReturn = argumentCaptor<Boolean>()

        verify(mockDeviceManagerListener, times(3)).onDeviceManagerErrorForAJobResult(
            captorJobResult.capture(),
            captorIsLastReturn.capture()
        )

        assertThat(captorJobResult.firstValue.isSuccessful, `is`(equalTo(false)))
        assertThat(captorJobResult.firstValue.scopeTag, `is`(equalTo(CONNECT)))
        assertThat(captorJobResult.firstValue.deviceManagerException?.error, `is`(equalTo(MAIN_JOB_IS_RUNNING)))
        assertThat(captorIsLastReturn.firstValue, `is`(equalTo(false)))
        assertThat(captorIsLastReturn.lastValue, `is`(equalTo(true)))

        //after
        deviceManager.mainJobIsRunning = false
    }

    @Test
    fun test_multiple_print_list_printer_data_main_job_is_running() {
        // given
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.printData(ticketData)
        deviceManager.printData(listOfTwoPrinterData, ticketData)

        // then
        val captorJobResult = argumentCaptor<DeviceManagerJobResult>()
        val captorIsLastReturn = argumentCaptor<Boolean>()

        verify(mockDeviceManagerListener, times(1)).onDeviceManagerErrorForAJobResult(
            captorJobResult.capture(),
            captorIsLastReturn.capture()
        )

        assertThat(captorJobResult.firstValue.isSuccessful, `is`(equalTo(false)))
        assertThat(captorJobResult.firstValue.scopeTag, `is`(equalTo(PRINT_DATA)))
        assertThat(captorJobResult.firstValue.deviceManagerException?.error, `is`(equalTo(PRINTER_DATA_NOT_MANAGED)))
        assertThat(captorIsLastReturn.firstValue, `is`(equalTo(false)))

        //after
        deviceManager.mainJobIsRunning = false
    }

    @Test
    fun test_multiple_disconnect_list_printer_data_main_job_is_running() {
        // given
        listOfPrinters[printerData] = mockPrinter

        // when
        deviceManager.disconnectPrinter()
        deviceManager.disconnectPrinter(listOfTwoPrinterData)

        // then
        val captorJobResult = argumentCaptor<DeviceManagerJobResult>()
        val captorIsLastReturn = argumentCaptor<Boolean>()

        verify(mockDeviceManagerListener, times(2)).onDeviceManagerErrorForAJobResult(
            captorJobResult.capture(),
            captorIsLastReturn.capture()
        )

        assertThat(captorJobResult.firstValue.isSuccessful, `is`(equalTo(false)))
        assertThat(captorJobResult.firstValue.scopeTag, `is`(equalTo(DISCONNECT)))
        assertThat(captorJobResult.firstValue.deviceManagerException?.error, `is`(equalTo(MAIN_JOB_IS_RUNNING)))
        assertThat(captorIsLastReturn.firstValue, `is`(equalTo(false)))
        assertThat(captorIsLastReturn.lastValue, `is`(equalTo(true)))

        //after
        deviceManager.mainJobIsRunning = false
    }
    //endregion MAIN_JOB_RUNNING

    //region GET_STATUS
    @Test
    fun test_get_managed_printer_list_two_printer_disconnected() {
        runBlocking {
            // given
            listOfPrinters[printerData] = mockPrinter
            listOfPrinters[printerDataSecond] = mockPrinterSecond

            val printerStatusDisconnected = PrinterStatus()
            whenever(mockPrinter.getStatusRaw()).thenReturn(printerStatusDisconnected)
            whenever(mockPrinterSecond.getStatusRaw()).thenReturn(printerStatusDisconnected)

            // when
            deviceManager.getManagedPrinterDataAndStatusList()

            // then
            val captorPrinterManagedList = argumentCaptor<ArrayList<PrinterManaged>>()
            verify(mockDeviceManagerListener, times(1)).onListOfPrinterManagedReceived(captorPrinterManagedList.capture())
            assertThat(captorPrinterManagedList.firstValue[0].printerStatus.connectionStatus.isConnected, `is`(equalTo(false)))
            assertThat(captorPrinterManagedList.firstValue[1].printerStatus.connectionStatus.isConnected, `is`(equalTo(false)))
        }
    }

    @Test
    fun test_get_managed_printer_list_one_printer_disconnected_and_one_printer_connected() {
        runBlocking {
            // given
            listOfPrinters[printerData] = mockPrinter
            listOfPrinters[printerDataSecond] = mockPrinterSecond

            val mockPrinterStatusInfo: PrinterStatusInfo = mock()
            whenever(mockPrinterStatusInfo.connection).thenReturn(com.epson.epos2.printer.Printer.TRUE)
            whenever(mockPrinterStatusInfo.online).thenReturn(com.epson.epos2.printer.Printer.TRUE)
            whenever(mockPrinterStatusInfo.coverOpen).thenReturn(com.epson.epos2.printer.Printer.FALSE)
            whenever(mockPrinterStatusInfo.paper).thenReturn(com.epson.epos2.printer.Printer.PAPER_OK)
            whenever(mockPrinterStatusInfo.paperFeed).thenReturn(com.epson.epos2.printer.Printer.TRUE)
            whenever(mockPrinterStatusInfo.panelSwitch).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)
            whenever(mockPrinterStatusInfo.drawer).thenReturn(com.epson.epos2.printer.Printer.DRAWER_HIGH)
            whenever(mockPrinterStatusInfo.errorStatus).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)
            whenever(mockPrinterStatusInfo.autoRecoverError).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)
            whenever(mockPrinterStatusInfo.buzzer).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)
            whenever(mockPrinterStatusInfo.adapter).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)
            whenever(mockPrinterStatusInfo.batteryLevel).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)

            val printerStatusDisconnected = PrinterStatus()
            val printerStatusConnected = PrinterStatus(mockPrinterStatusInfo)
            whenever(mockPrinter.getStatusRaw()).thenReturn(printerStatusDisconnected)
            whenever(mockPrinterSecond.getStatusRaw()).thenReturn(printerStatusConnected)

            // when
            deviceManager.getManagedPrinterDataAndStatusList()

            // then
            val captorPrinterManagedList = argumentCaptor<MutableList<PrinterManaged>>()
            verify(mockDeviceManagerListener, times(1)).onListOfPrinterManagedReceived(captorPrinterManagedList.capture())

            val counterConnectedPrinter =
                captorPrinterManagedList.firstValue.filter { it.printerStatus.connectionStatus.isConnected == true }.size
            val counterDisconnectedPrinter =
                captorPrinterManagedList.firstValue.filter { it.printerStatus.connectionStatus.isConnected == false }.size
            assertThat(counterConnectedPrinter, `is`(equalTo(1)))
            assertThat(counterDisconnectedPrinter, `is`(equalTo(1)))
        }
    }

    @Test
    fun test_get_managed_printer_list_two_printer_connected() {
        runBlocking {
            // given
            listOfPrinters[printerData] = mockPrinter
            listOfPrinters[printerDataSecond] = mockPrinterSecond

            val mockPrinterStatusInfo: PrinterStatusInfo = mock()
            whenever(mockPrinterStatusInfo.connection).thenReturn(com.epson.epos2.printer.Printer.TRUE)
            whenever(mockPrinterStatusInfo.online).thenReturn(com.epson.epos2.printer.Printer.TRUE)
            whenever(mockPrinterStatusInfo.coverOpen).thenReturn(com.epson.epos2.printer.Printer.FALSE)
            whenever(mockPrinterStatusInfo.paper).thenReturn(com.epson.epos2.printer.Printer.PAPER_OK)
            whenever(mockPrinterStatusInfo.paperFeed).thenReturn(com.epson.epos2.printer.Printer.TRUE)
            whenever(mockPrinterStatusInfo.panelSwitch).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)
            whenever(mockPrinterStatusInfo.drawer).thenReturn(com.epson.epos2.printer.Printer.DRAWER_HIGH)
            whenever(mockPrinterStatusInfo.errorStatus).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)
            whenever(mockPrinterStatusInfo.autoRecoverError).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)
            whenever(mockPrinterStatusInfo.buzzer).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)
            whenever(mockPrinterStatusInfo.adapter).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)
            whenever(mockPrinterStatusInfo.batteryLevel).thenReturn(com.epson.epos2.printer.Printer.UNKNOWN)

            val printerStatusConnected = PrinterStatus(mockPrinterStatusInfo)
            whenever(mockPrinter.getStatusRaw()).thenReturn(printerStatusConnected)
            whenever(mockPrinterSecond.getStatusRaw()).thenReturn(printerStatusConnected)

            // when
            deviceManager.getManagedPrinterDataAndStatusList()

            // then
            val captorPrinterManagedList = argumentCaptor<ArrayList<PrinterManaged>>()
            verify(mockDeviceManagerListener, times(1)).onListOfPrinterManagedReceived(captorPrinterManagedList.capture())
            assertThat(captorPrinterManagedList.firstValue[0].printerStatus.connectionStatus.isConnected, `is`(equalTo(true)))
            assertThat(captorPrinterManagedList.firstValue[1].printerStatus.connectionStatus.isConnected, `is`(equalTo(true)))
        }
    }
    //endregion GET_STATUS
}
