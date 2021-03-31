package com.tabesto.printer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.epson.epos2.Epos2Exception
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.tabesto.printer.dagger.DaggerPrinterEpsonComponentTest
import com.tabesto.printer.dagger.PrinterEpsonModuleTest
import com.tabesto.printer.model.ConnectionMode
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterModel
import com.tabesto.printer.model.PrinterRegion
import com.tabesto.printer.model.PrinterType
import com.tabesto.printer.model.error.EposException
import com.tabesto.printer.model.status.ConnectionStatus
import com.tabesto.printer.model.status.PrinterStatus
import com.tabesto.printer.model.ticket.FeedLine
import com.tabesto.printer.model.ticket.StringLine
import com.tabesto.printer.model.ticket.StyleLine
import com.tabesto.printer.model.ticket.TicketData
import com.tabesto.printer.utils.EposPrinter
import com.tabesto.printer.writer.PrinterWriter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

/**
 * PrinterEpsonTest class contains all unit test for PrinterEspon class
 * it implements test for each public method contained in Printer interface
 * all test ran by this class is instrumented (@RunWith(AndroidJUnit4::class)) because we need to mock : EposPrinter
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class PrinterEpsonTest {
    private val mockEposPrinter: EposPrinter = mock()
    private val mockPrinterConnectListener: PrinterConnectListener = mock()
    private val mockPrinterStatusListener: PrinterStatusListener = mock()
    private val mockPrinterWriter: PrinterWriter = mock()
    private val context: Context = ApplicationProvider.getApplicationContext<Context>()
    private val mockEpos2Exception: Epos2Exception = mock()
    private val mockPrinterPrintListener: PrinterPrintListener = mock()

    companion object {
        private const val PRINTER_ADDRESS = "BT:00:01:90:BE:3D:69"
        private val ticketDataBuilder = TicketData.TicketDataBuilder()
        private val ticketData: TicketData = ticketDataBuilder
            .withFeedLine(FeedLine())
            .withLine(StringLine("THE STORE 123 (555) 555 – 5555", StyleLine()))
            .withFeedLine(FeedLine()).build()
        private val exception = Exception("some error message")
    }

    private lateinit var printerEpson: PrinterEpson

    private val printerData: PrinterData = PrinterData(
        PrinterModel.PRINTER_TM_M10,
        PrinterRegion.PRINTER_ANK,
        PRINTER_ADDRESS,
        ConnectionMode.AUTO,
        PrinterType.PRINTER_EPSON
    )

    @Before
    fun setUp() {
        // Given
        printerEpson = PrinterEpson(printerData, context)
        printerEpson.initializePrinter()
        printerEpson.setConnectListener(mockPrinterConnectListener)
        printerEpson.setPrintListener(mockPrinterPrintListener)
        printerEpson.setStatusListener(mockPrinterStatusListener)

        DaggerPrinterEpsonComponentTest.builder()
            .printerEpsonModuleTest(PrinterEpsonModuleTest(mockEposPrinter, mockPrinterWriter)).build()
            .inject(printerEpson)
    }

    @Test
    fun test_initializePrinter_successful() {
        // when
        val result: Unit = printerEpson.initializePrinter()

        // then
        assertThat(result, `is`(equalTo(Unit)))
    }

    @Test
    fun test_connectPrinter_with_callback_successful() {
        runBlocking {
            // when
            printerEpson.connectPrinter()

            // then
            verify(mockPrinterConnectListener, times(1)).onConnectSuccess(eq(printerData))
        }
    }

    @Test
    fun test_connectPrinter_with_callback_failure_with_epos2Exception() {
        runBlocking {
            // given
            whenever(mockEpos2Exception.errorStatus).doReturn(EposException.EPOS_EXCEPTION_ERR_ILLEGAL.codeInt)
            whenever(mockEposPrinter.connect(printerData.printerAddress, EposPrinter.PARAM_DEFAULT)).doThrow(mockEpos2Exception)

            // when
            printerEpson.connectPrinter()

            // then
            verify(mockPrinterConnectListener, times(1)).onConnectFailure(eq(printerData), any())
        }
    }

    @Test
    fun test_connectPrinter_with_callback_failure_with_exception() {
        runBlocking {
            // given
            whenever(mockEposPrinter.connect(printerData.printerAddress, EposPrinter.PARAM_DEFAULT)).doAnswer { throw exception }

            // when
            printerEpson.connectPrinter()

            // then
            verify(mockPrinterConnectListener, times(1)).onConnectFailure(eq(printerData), any())
        }
    }

    @Test
    fun test_disconnectPrinter_with_callback_successful() {
        runBlocking {
            // when
            printerEpson.disconnectPrinter()

            // then
            verify(mockPrinterConnectListener, times(1)).onDisconnectSuccess(eq(printerData))
        }
    }

    @Test
    fun test_disconnectPrinter_with_callback__failure_with_epos2Exception() {
        runBlocking {
            // given
            whenever(mockEpos2Exception.errorStatus).doReturn(EposException.EPOS_EXCEPTION_ERR_ILLEGAL.codeInt)
            whenever(mockEposPrinter.disconnect()).doThrow(mockEpos2Exception)

            // when
            printerEpson.disconnectPrinter()

            // then
            verify(mockPrinterConnectListener, times(1)).onDisconnectFailure(eq(printerData), any())
        }
    }

    @Test
    fun test_disconnectPrinter_with_callback_failure_with_exception() {
        runBlocking {
            // given
            whenever(mockEposPrinter.disconnect()).doAnswer { throw exception }

            // when
            printerEpson.disconnectPrinter()

            // then
            verify(mockPrinterConnectListener, times(1)).onDisconnectFailure(eq(printerData), any())
        }
    }

    @Test
    fun test_printData_successful() {
        //when
        val result: Unit = printerEpson.printData(ticketData)

        // then
        assertThat(result, `is`(equalTo(Unit)))
    }

    @Test
    fun test_printData_failure_with_epos2Exception() {
        // given
        whenever(mockEpos2Exception.errorStatus).doReturn(EposException.EPOS_EXCEPTION_ERR_ILLEGAL.codeInt)
        whenever(mockEposPrinter.sendData(any())).doThrow(mockEpos2Exception)

        // when
        printerEpson.printData(ticketData)

        //then
        verify(mockPrinterPrintListener, times(1)).onPrintDataFailure(eq(printerData), any())
    }

    @Test
    fun test_printData_with_Failure_with_exception() {
        // given
        whenever(mockEposPrinter.sendData(any())).doAnswer { throw exception }

        // when
        printerEpson.printData(ticketData)

        //then
        verify(mockPrinterPrintListener, times(1)).onPrintDataFailure(eq(printerData), any())
    }

    @Test
    fun test_getStatus_with_callback_printer_disconnected() {
        // given
        val mockPrinterStatusInfo: PrinterStatusInfo = mock()
        whenever(mockPrinterStatusInfo.connection).thenReturn(Printer.FALSE)
        whenever(mockEposPrinter.status).doAnswer { mockPrinterStatusInfo }

        // when
        printerEpson.getStatus()

        // then
        val captorPrinterStatus = argumentCaptor<PrinterStatus>()

        verify(mockPrinterStatusListener, times(1)).onStatusReceived(eq(printerData), captorPrinterStatus.capture())
        assertThat(captorPrinterStatus.firstValue.connectionStatus, `is`(equalTo(ConnectionStatus.DISCONNECTED)))
    }

    @Test
    fun test_getStatus_with_callback_printer_connected() {
        // given
        val mockPrinterStatusInfo: PrinterStatusInfo = mock()
        whenever(mockPrinterStatusInfo.connection).thenReturn(Printer.TRUE)
        whenever(mockPrinterStatusInfo.online).thenReturn(Printer.TRUE)
        whenever(mockPrinterStatusInfo.coverOpen).thenReturn(Printer.FALSE)
        whenever(mockPrinterStatusInfo.paper).thenReturn(Printer.PAPER_OK)
        whenever(mockPrinterStatusInfo.paperFeed).thenReturn(Printer.TRUE)
        whenever(mockPrinterStatusInfo.panelSwitch).thenReturn(Printer.UNKNOWN)
        whenever(mockPrinterStatusInfo.drawer).thenReturn(Printer.DRAWER_HIGH)
        whenever(mockPrinterStatusInfo.errorStatus).thenReturn(Printer.UNKNOWN)
        whenever(mockPrinterStatusInfo.autoRecoverError).thenReturn(Printer.UNKNOWN)
        whenever(mockPrinterStatusInfo.buzzer).thenReturn(Printer.UNKNOWN)
        whenever(mockPrinterStatusInfo.adapter).thenReturn(Printer.UNKNOWN)
        whenever(mockPrinterStatusInfo.batteryLevel).thenReturn(Printer.UNKNOWN)
        whenever(mockEposPrinter.status).doAnswer { mockPrinterStatusInfo }

        // when
        printerEpson.getStatus()

        // then
        val captorPrinterStatus = argumentCaptor<PrinterStatus>()

        verify(mockPrinterStatusListener, times(1)).onStatusReceived(eq(printerData), captorPrinterStatus.capture())
        assertThat(captorPrinterStatus.firstValue.connectionStatus, `is`(equalTo(ConnectionStatus.CONNECTED)))
    }

    /**
     * This test won't pass because we have to mock the context inside BluetoothHelper and this will make us
     * to modify its constructor
     *
     */
    /*@Test
    fun test_RestartBluetooth_Fail() {
        // given
        val mockBluetoothHelper: BluetoothHelper = mock()
        val context: Context = ApplicationProvider.getApplicationContext<Context>()

        printerData =
            PrinterData(
                PrinterModel.PRINTER_TM_M10,
                PrinterRegion.PRINTER_ANK,
                PRINTER_ADDRESS,
                ConnectionMode.AUTO,
                PrinterType.PRINTER_EPSON,
                context
            )

        printerData?.let { printerData ->
            printerEpson = PrinterEpson(printerData)
        }

        // when
        whenever(mockBluetoothHelper?.resetBluetoothModule()).doAnswer { Unit }

        var result: Unit = printerEpson.restartBluetooth()

        // then
        assertThat(result, `is`(equalTo(Unit)))
    }*/

    /**
     * This test won't pass because we have to mock the context inside BluetoothHelper and this will make us
     * to modify its constructor
     */
    /*@Test
    fun test_RestartBluetoothAndLaunchDiscovery() {
        // given
        val context: Context = ApplicationProvider.getApplicationContext<Context>()
        val mockBluetoothHelper: BluetoothHelper = mock<BluetoothHelper>()

        printerData =
            PrinterData(
                PrinterModel.PRINTER_TM_M10,
                PrinterRegion.PRINTER_ANK,
                PRINTER_ADDRESS,
                ConnectionMode.AUTO,
                PrinterType.PRINTER_EPSON,
                context
            )

        printerData?.let { printerData ->
            printerEpson = PrinterEpson(printerData)
        }

        // when
        whenever(context?.registerReceiver(any(), any())).thenAnswer { Unit }
        whenever(mockBluetoothHelper?.resetBluetoothModule()).doAnswer { Unit }

        var result: Unit = printerEpson.restartBluetoothAndLaunchDiscovery()

        // then
        assertThat(result, `is`(equalTo(Unit)))
    }*/
}
