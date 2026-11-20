package com.andrerinas.openheadunit.main

import com.andrerinas.openheadunit.main.ConnectionReadinessPolicy.Problem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConnectionReadinessPolicyTest {

    private val ready = ConnectionReadinessPolicy.Input(
        nativeWireless = true,
        hotspotTransport = false,
        sessionConnected = false,
        onboardingComplete = true,
        wirelessPermissionsMissing = false,
        bluetoothOn = true,
        pairedPhones = 1,
        wifiOn = true,
        locationOn = true,
        locationGatesWifiDirect = true,
        hotspotOn = false,
        hotspotStartsItself = false,
    )

    private fun first(input: ConnectionReadinessPolicy.Input) = ConnectionReadinessPolicy.firstProblem(input)

    @Test
    fun `a unit with everything on has nothing to report`() {
        assertNull(first(ready))
    }

    @Test
    fun `nothing is reported for cable-only units, during a session or before setup`() {
        val broken = ready.copy(bluetoothOn = false, wifiOn = false)
        assertNull(first(broken.copy(nativeWireless = false)))
        assertNull(first(broken.copy(sessionConnected = true)))
        assertNull(first(broken.copy(onboardingComplete = false)))
    }

    @Test
    fun `problems come in the order they have to be fixed`() {
        val all = ready.copy(
            wirelessPermissionsMissing = true, bluetoothOn = false, pairedPhones = 0,
            hotspotOn = true, wifiOn = false, locationOn = false
        )
        assertEquals(Problem.PERMISSIONS_MISSING, first(all))
        assertEquals(Problem.BLUETOOTH_OFF, first(all.copy(wirelessPermissionsMissing = false)))
        assertEquals(Problem.NO_PAIRED_PHONE, first(all.copy(wirelessPermissionsMissing = false, bluetoothOn = true)))
        val paired = all.copy(wirelessPermissionsMissing = false, bluetoothOn = true, pairedPhones = 2)
        assertEquals(Problem.HOTSPOT_ON, first(paired))
        assertEquals(Problem.WIFI_OFF, first(paired.copy(hotspotOn = false)))
        assertEquals(Problem.LOCATION_OFF, first(paired.copy(hotspotOn = false, wifiOn = true)))
    }

    @Test
    fun `location only matters where Android ties WiFi Direct to it`() {
        assertNull(first(ready.copy(locationOn = false, locationGatesWifiDirect = false)))
    }

    @Test
    fun `the hotspot transport wants the hotspot on, unless the app starts it`() {
        val hotspot = ready.copy(hotspotTransport = true, hotspotOn = false, wifiOn = false, locationOn = false)
        assertEquals(Problem.HOTSPOT_OFF, first(hotspot))
        assertNull(first(hotspot.copy(hotspotStartsItself = true)))
        assertNull(first(hotspot.copy(hotspotOn = true)))
    }

    @Test
    fun `a reading that failed is never reported`() {
        assertNull(first(ready.copy(bluetoothOn = null, pairedPhones = null, wifiOn = null, locationOn = null, hotspotOn = null)))
    }
}
