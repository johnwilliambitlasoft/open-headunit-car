package com.andrerinas.openheadunit.main

import com.andrerinas.openheadunit.connection.ConnectionStage
import com.andrerinas.openheadunit.main.HomePhoneStepsPolicy.State.CURRENT
import com.andrerinas.openheadunit.main.HomePhoneStepsPolicy.State.DONE
import com.andrerinas.openheadunit.main.HomePhoneStepsPolicy.State.TODO
import org.junit.Assert.assertEquals
import org.junit.Test

class HomePhoneStepsPolicyTest {

    @Test
    fun `wireless walks Bluetooth, then WiFi, then Android Auto`() {
        assertEquals(listOf(CURRENT, TODO, TODO), HomePhoneStepsPolicy.wireless(false, null))
        assertEquals(listOf(CURRENT, TODO, TODO), HomePhoneStepsPolicy.wireless(false, ConnectionStage.WAKING_PHONE))
        assertEquals(listOf(DONE, CURRENT, TODO), HomePhoneStepsPolicy.wireless(false, ConnectionStage.PHONE_ANSWERED))
        assertEquals(listOf(DONE, CURRENT, TODO), HomePhoneStepsPolicy.wireless(false, ConnectionStage.PHONE_JOINING))
        assertEquals(listOf(DONE, DONE, CURRENT), HomePhoneStepsPolicy.wireless(false, ConnectionStage.CONNECTING))
        assertEquals(listOf(DONE, DONE, CURRENT), HomePhoneStepsPolicy.wireless(false, ConnectionStage.STARTING_PROJECTION))
        assertEquals(listOf(DONE, DONE, DONE), HomePhoneStepsPolicy.wireless(true, null))
    }

    @Test
    fun `the cable step is done once a phone is attached`() {
        assertEquals(listOf(CURRENT, TODO, TODO), HomePhoneStepsPolicy.usb(false, null, phoneAttached = false))
        assertEquals(listOf(DONE, CURRENT, TODO), HomePhoneStepsPolicy.usb(false, null, phoneAttached = true))
        assertEquals(listOf(DONE, CURRENT, TODO), HomePhoneStepsPolicy.usb(false, ConnectionStage.USB_SWITCHING, false))
        assertEquals(listOf(DONE, DONE, CURRENT), HomePhoneStepsPolicy.usb(false, ConnectionStage.SECURING, true))
        assertEquals(listOf(DONE, DONE, DONE), HomePhoneStepsPolicy.usb(true, null, true))
    }
}
