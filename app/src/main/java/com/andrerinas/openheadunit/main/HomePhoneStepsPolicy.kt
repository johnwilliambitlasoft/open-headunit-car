package com.andrerinas.openheadunit.main

import com.andrerinas.openheadunit.connection.ConnectionStage

/**
 * Which of the three "On your phone" steps on the landscape home card is done, which one the
 * connection is on now, and which are still ahead.
 *
 * Wireless: 1 Bluetooth (the phone is woken and answers), 2 WiFi (it gets the network and joins),
 * 3 Android Auto (the session comes up). Cable: 1 the cable (a phone is attached), 2 the phone
 * (it switches to accessory mode and is allowed), 3 Android Auto.
 *
 * Pure so the mapping from the connection stages can be tested.
 */
object HomePhoneStepsPolicy {

    enum class State { DONE, CURRENT, TODO }

    private val CURRENT_FIRST = listOf(State.CURRENT, State.TODO, State.TODO)
    private val CURRENT_SECOND = listOf(State.DONE, State.CURRENT, State.TODO)
    private val CURRENT_THIRD = listOf(State.DONE, State.DONE, State.CURRENT)
    private val ALL_DONE = listOf(State.DONE, State.DONE, State.DONE)

    fun wireless(sessionActive: Boolean, stage: ConnectionStage?): List<State> = when {
        sessionActive -> ALL_DONE
        stage == null || stage.rank < ConnectionStage.PHONE_ANSWERED.rank -> CURRENT_FIRST
        stage.rank < ConnectionStage.CONNECTING.rank -> CURRENT_SECOND
        else -> CURRENT_THIRD
    }

    fun usb(sessionActive: Boolean, stage: ConnectionStage?, phoneAttached: Boolean): List<State> = when {
        sessionActive -> ALL_DONE
        stage != null && stage.rank >= ConnectionStage.CONNECTING.rank -> CURRENT_THIRD
        phoneAttached || stage == ConnectionStage.USB_ATTACHED || stage == ConnectionStage.USB_SWITCHING ->
            CURRENT_SECOND
        else -> CURRENT_FIRST
    }
}
