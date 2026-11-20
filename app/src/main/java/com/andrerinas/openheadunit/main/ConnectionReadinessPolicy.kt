package com.andrerinas.openheadunit.main

/**
 * The one thing on this unit that stops a wireless connection right now, read from its live state.
 *
 * The connection issue banner only ever said what went wrong last time. A switch that is off now
 * (Bluetooth, WiFi, location) or a hotspot left on is the reason the next attempt will fail, and
 * the user could not tell which one, so this is asked first and shown ahead of the recorded
 * failures. One answer at a time, in the order a user has to fix them: a later step cannot work
 * until the earlier one does.
 *
 * Pure so the order can be tested; MainActivity reads the inputs.
 */
object ConnectionReadinessPolicy {

    enum class Problem {
        /** Location or nearby-devices permission not granted: the wireless stack cannot start. */
        PERMISSIONS_MISSING,
        /** The phone is woken over Bluetooth. */
        BLUETOOTH_OFF,
        /** Bluetooth is on but no phone was ever paired with this unit. */
        NO_PAIRED_PHONE,
        /** WiFi Direct needs the WiFi radio on, even with no network joined. */
        WIFI_OFF,
        /** Android 10-12 hide WiFi Direct details from apps while location is off. */
        LOCATION_OFF,
        /** A running hotspot holds the radio WiFi Direct needs. */
        HOTSPOT_ON,
        /** The hotspot transport needs this unit's hotspot running, and nothing here starts it. */
        HOTSPOT_OFF,
    }

    /** Null in any reading means it could not be read, which is never reported as a problem. */
    data class Input(
        /** Wireless is chosen and the mode is Native Android Auto. */
        val nativeWireless: Boolean,
        /** The Native transport is this unit's hotspot rather than WiFi Direct. */
        val hotspotTransport: Boolean,
        val sessionConnected: Boolean,
        val onboardingComplete: Boolean,
        val wirelessPermissionsMissing: Boolean,
        val bluetoothOn: Boolean?,
        val pairedPhones: Int?,
        val wifiOn: Boolean?,
        val locationOn: Boolean?,
        /** Only Android 10-12 tie WiFi Direct to the location switch. */
        val locationGatesWifiDirect: Boolean,
        val hotspotOn: Boolean?,
        /** The app turns the hotspot on by itself, so it being off while idle is normal. */
        val hotspotStartsItself: Boolean,
    )

    fun firstProblem(input: Input): Problem? {
        if (!input.nativeWireless || input.sessionConnected || !input.onboardingComplete) return null
        val wifiDirect = !input.hotspotTransport
        return when {
            input.wirelessPermissionsMissing -> Problem.PERMISSIONS_MISSING
            input.bluetoothOn == false -> Problem.BLUETOOTH_OFF
            input.pairedPhones == 0 -> Problem.NO_PAIRED_PHONE
            wifiDirect && input.hotspotOn == true -> Problem.HOTSPOT_ON
            wifiDirect && input.wifiOn == false -> Problem.WIFI_OFF
            wifiDirect && input.locationGatesWifiDirect && input.locationOn == false -> Problem.LOCATION_OFF
            !wifiDirect && input.hotspotOn == false && !input.hotspotStartsItself -> Problem.HOTSPOT_OFF
            else -> null
        }
    }
}
