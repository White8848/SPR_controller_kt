package com.example.sprcontrollerkt.gamepad

import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import kotlin.math.abs

class GamePad {

    private var mKeyCode = 0

    private var buttonA = 0
    private var buttonB = 0
    private var buttonX = 0
    private var buttonY = 0
    private var buttonL1 = 0
    private var buttonL3 = 0
    private var buttonR1 = 0
    private var buttonR3 = 0
    private var buttonSelect = 0
    private var buttonStart = 0
    private var buttonUp = 0
    private var buttonDown = 0
    private var buttonLeft = 0
    private var buttonRight = 0

    private var axisX = 0f
    private var axisY = 0f
    private var axisZ = 0f
    private var axisRZ = 0f
    private var axisLT = 0f
    private var axisRT = 0f


    init {
        Log.i("GamePad", "初始化手柄")
    }

    //读取按键信息
    fun setKeyCode(KeyCode: Int) {
        this.mKeyCode = KeyCode
    }

    //更新按键信息
    fun updateKeyValue(value: Int) {
        when (this.mKeyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> {
                buttonA = value
            }
            KeyEvent.KEYCODE_BUTTON_B -> {
                buttonB = value
            }
            KeyEvent.KEYCODE_BUTTON_X -> {
                buttonX = value
            }
            KeyEvent.KEYCODE_BUTTON_Y -> {
                buttonY = value
            }
            KeyEvent.KEYCODE_BUTTON_L1 -> {
                buttonL1 = value
            }
            KeyEvent.KEYCODE_BUTTON_R1 -> {
                buttonR1 = value
            }
            KeyEvent.KEYCODE_BUTTON_THUMBL -> {
                buttonL3 = value
            }
            KeyEvent.KEYCODE_BUTTON_THUMBR -> {
                buttonR3 = value
            }
            KeyEvent.KEYCODE_BUTTON_SELECT -> {
                buttonSelect = value
            }
            KeyEvent.KEYCODE_BUTTON_START -> {
                buttonStart = value
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                buttonUp = value
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                buttonDown = value
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                buttonLeft = value
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                buttonRight = value
            }
        }
    }

    private fun getCenteredAxis(
        event: MotionEvent,
        device: InputDevice,
        axis: Int,
        historyPos: Int
    ): Float {
        val range: InputDevice.MotionRange? = device.getMotionRange(axis, event.source)

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        range?.apply {
            val value: Float = if (historyPos < 0) {
                event.getAxisValue(axis)
            } else {
                event.getHistoricalAxisValue(axis, historyPos)
            }

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (abs(value) > flat) {
                return value
            }
        }
        return 0f
    }

    fun processJoystickInput(event: MotionEvent, historyPos: Int) {

        val inputDevice = event.device

        axisX = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X, historyPos)

        axisY = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y, historyPos)

        axisZ = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Z, historyPos)

        axisRZ = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RZ, historyPos)

        axisLT = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_LTRIGGER, historyPos)

        axisRT = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RTRIGGER, historyPos)

        // Update the ship object based on the new x and y values
    }

    fun getButton(): String {
        return "${this.buttonA}:${this.buttonB}:${this.buttonX}:${this.buttonY}"
    }

    fun getJoystick():String{
        return "${this.axisX}:${this.axisY}:${this.axisZ}:${this.axisRZ}:${this.axisLT}:${this.axisRT}"
    }
}