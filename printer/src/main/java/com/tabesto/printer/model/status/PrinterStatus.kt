package com.tabesto.printer.model.status

import android.os.Parcelable
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PrinterStatus(
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val onlineStatus: OnlineStatus = OnlineStatus.UNKNOWN,
    val coverOpenStatus: CoverOpenStatus = CoverOpenStatus.UNKNOWN,
    val paperStatus: PaperStatus = PaperStatus.UNKNOWN,
    val paperFeedStatus: PaperFeedStatus = PaperFeedStatus.UNKNOWN,
    val panelSwitchStatus: PanelSwitchStatus = PanelSwitchStatus.UNKNOWN,
    val drawerStatus: DrawerStatus = DrawerStatus.UNKNOWN,
    val errorStatus: ErrorStatus = ErrorStatus.UNKNOWN,
    val autoRecoveryErrorStatus: AutoRecoveryErrorStatus = AutoRecoveryErrorStatus.UNKNOWN,
    val buzzerSoundStatus: BuzzerSoundStatus = BuzzerSoundStatus.UNKNOWN,
    val powerAdapterStatus: PowerAdapterStatus = PowerAdapterStatus.UNKNOWN,
    val batteryLevelStatus: BatteryLevelStatus = BatteryLevelStatus.UNKNOWN
) : Parcelable {
    constructor(printerStatusInfo: PrinterStatusInfo?) : this(
        ConnectionStatus.valueOf(printerStatusInfo?.connection),
        OnlineStatus.valueOf(printerStatusInfo?.online),
        CoverOpenStatus.valueOf(printerStatusInfo?.coverOpen),
        PaperStatus.valueOf(printerStatusInfo?.paper),
        PaperFeedStatus.valueOf(printerStatusInfo?.paperFeed),
        PanelSwitchStatus.valueOf(printerStatusInfo?.panelSwitch),
        DrawerStatus.valueOf(printerStatusInfo?.drawer),
        ErrorStatus.valueOf(printerStatusInfo?.errorStatus),
        AutoRecoveryErrorStatus.valueOf(printerStatusInfo?.autoRecoverError),
        BuzzerSoundStatus.valueOf(printerStatusInfo?.buzzer),
        PowerAdapterStatus.valueOf(printerStatusInfo?.adapter),
        BatteryLevelStatus.valueOf(printerStatusInfo?.batteryLevel)
    )
}

@Suppress("unused")
enum class ConnectionStatus(val eposPrinterStatusInfo: Int, val isConnected: Boolean?) {
    CONNECTED(Printer.TRUE, true),
    DISCONNECTED(Printer.FALSE, false);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<ConnectionStatus>().firstOrNull { connectionStatus ->
                connectionStatus.eposPrinterStatusInfo == eposPrinterStatusInfo
            }
                ?: DISCONNECTED
    }
}

@Suppress("unused")
enum class OnlineStatus(val eposPrinterStatusInfo: Int, val isOnline: Boolean?) {
    ONLINE(Printer.TRUE, true),
    OFFLINE(Printer.FALSE, false),
    UNKNOWN(Printer.UNKNOWN, null);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<OnlineStatus>().firstOrNull { onlineStatus -> onlineStatus.eposPrinterStatusInfo == eposPrinterStatusInfo }
                ?: UNKNOWN
    }
}

@Suppress("unused")
enum class CoverOpenStatus(val eposPrinterStatusInfo: Int, val isOpen: Boolean?) {
    OPEN(Printer.TRUE, true),
    CLOSE(Printer.FALSE, false),
    UNKNOWN(Printer.UNKNOWN, null);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<CoverOpenStatus>().firstOrNull { coverOpenStatus -> coverOpenStatus.eposPrinterStatusInfo == eposPrinterStatusInfo }
                ?: UNKNOWN
    }
}

@Suppress("unused")
enum class PaperStatus(val eposPrinterStatusInfo: Int) {
    OK(Printer.PAPER_OK),
    NEAR_END(Printer.PAPER_NEAR_END),
    EMPTY(Printer.PAPER_EMPTY),
    UNKNOWN(Printer.UNKNOWN);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<PaperStatus>().firstOrNull { paperStatus -> paperStatus.eposPrinterStatusInfo == eposPrinterStatusInfo } ?: UNKNOWN
    }
}

@Suppress("unused")
enum class PaperFeedStatus(val eposPrinterStatusInfo: Int, val isInProgress: Boolean?) {
    IN_PROGRESS(Printer.TRUE, true),
    CLOSE(Printer.FALSE, false),
    UNKNOWN(Printer.UNKNOWN, null);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<PaperFeedStatus>().firstOrNull { paperFeedStatus -> paperFeedStatus.eposPrinterStatusInfo == eposPrinterStatusInfo }
                ?: UNKNOWN
    }
}

@Suppress("unused")
enum class PanelSwitchStatus(val eposPrinterStatusInfo: Int, val isPressed: Boolean?) {
    ON(Printer.SWITCH_ON, true),
    OFF(Printer.SWITCH_OFF, false),
    UNKNOWN(Printer.UNKNOWN, null);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) = enumValues<PanelSwitchStatus>()
            .firstOrNull { panelSwitchStatus -> panelSwitchStatus.eposPrinterStatusInfo == eposPrinterStatusInfo } ?: UNKNOWN
    }
}

@Suppress("unused")
enum class DrawerStatus(val eposPrinterStatusInfo: Int, val isHigh: Boolean?) {
    HIGH(Printer.DRAWER_HIGH, true),
    LOW(Printer.DRAWER_LOW, false),
    UNKNOWN(Printer.UNKNOWN, null);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<DrawerStatus>().firstOrNull { drawerStatus -> drawerStatus.eposPrinterStatusInfo == eposPrinterStatusInfo }
                ?: UNKNOWN
    }
}

@Suppress("unused")
enum class ErrorStatus(val eposPrinterStatusInfo: Int) {
    NORMAL(Printer.NO_ERR),
    MECHANICAL(Printer.MECHANICAL_ERR),
    AUTO_CUTTER(Printer.AUTOCUTTER_ERR),
    UNRECOVERABLE(Printer.UNRECOVER_ERR),
    AUTO_RECOVERY(Printer.AUTORECOVER_ERR),
    UNKNOWN(Printer.UNKNOWN);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<ErrorStatus>().firstOrNull { errorStatus -> errorStatus.eposPrinterStatusInfo == eposPrinterStatusInfo } ?: UNKNOWN
    }
}

@Suppress("unused")
enum class AutoRecoveryErrorStatus(val eposPrinterStatusInfo: Int) {
    HEAD_OVERHEAT(Printer.HEAD_OVERHEAT),
    MOTOR_OVERHEAT(Printer.MOTOR_OVERHEAT),
    BATTERY_OVERHEAT(Printer.BATTERY_OVERHEAT),
    WRONG_PAPER(Printer.WRONG_PAPER),
    COVER_OPEN(Printer.COVER_OPEN),
    UNKNOWN(Printer.UNKNOWN);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<AutoRecoveryErrorStatus>().firstOrNull { autoRecoveryErrorStatus ->
                autoRecoveryErrorStatus.eposPrinterStatusInfo == eposPrinterStatusInfo }
                ?: UNKNOWN
    }
}

@Suppress("unused")
enum class BuzzerSoundStatus(val eposPrinterStatusInfo: Int, val isSounding: Boolean?) {
    SOUNDING(Printer.TRUE, true),
    STOPPED(Printer.FALSE, false),
    UNKNOWN(Printer.UNKNOWN, null);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<BuzzerSoundStatus>().firstOrNull { buzzerSoundStatus ->
                buzzerSoundStatus.eposPrinterStatusInfo == eposPrinterStatusInfo }
                ?: UNKNOWN
    }
}

@Suppress("unused")
enum class PowerAdapterStatus(val eposPrinterStatusInfo: Int, val isConnected: Boolean?) {
    CONNECTED(Printer.TRUE, true),
    DISCONNECTED(Printer.FALSE, false),
    UNKNOWN(Printer.UNKNOWN, null);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<PowerAdapterStatus>().firstOrNull { powerAdapterStatus ->
                powerAdapterStatus.eposPrinterStatusInfo == eposPrinterStatusInfo }
                ?: UNKNOWN
    }
}

@Suppress("unused")
enum class BatteryLevelStatus(val eposPrinterStatusInfo: Int) {
    LEVEL_6(Printer.BATTERY_LEVEL_6),
    LEVEL_5(Printer.BATTERY_LEVEL_5),
    LEVEL_4(Printer.BATTERY_LEVEL_4),
    LEVEL_3(Printer.BATTERY_LEVEL_3),
    LEVEL_2(Printer.BATTERY_LEVEL_2),
    LEVEL_1(Printer.BATTERY_LEVEL_1),
    LEVEL_0(Printer.BATTERY_LEVEL_0),
    UNKNOWN(Printer.UNKNOWN);

    companion object {
        fun valueOf(eposPrinterStatusInfo: Int?) =
            enumValues<BatteryLevelStatus>().firstOrNull { batteryLevelStatus ->
                batteryLevelStatus.eposPrinterStatusInfo == eposPrinterStatusInfo }
                ?: UNKNOWN
    }
}
