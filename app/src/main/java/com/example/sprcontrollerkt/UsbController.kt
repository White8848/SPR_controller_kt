package com.example.sprcontrollerkt

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sprcontrollerkt.gamepad.GamePad
import com.example.sprcontrollerkt.udp.UdpClient

class UsbController : AppCompatActivity() {
    private val client: UdpClient = UdpClient()
    private var gamePad = GamePad()
    private var stopSend = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) //设置全屏
        setContentView(R.layout.activity_usb_controller)
    }

    fun changeController(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            gamePad.setKeyCode(keyCode)
            gamePad.updateKeyValue(1)
            true
        } else {
            super.onKeyUp(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return if (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            gamePad.setKeyCode(keyCode)
            gamePad.updateKeyValue(0)
            true
        } else {
            super.onKeyUp(keyCode, event)
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        // Check that the event came from a game controller
        return if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
            && event.action == MotionEvent.ACTION_MOVE
        ) {

            // Process the movements starting from the
            // earliest historical position in the batch
//            (0 until event.historySize).forEach { i ->
//                // Process the event at historical position i
//                gamePad.processJoystickInput(event, i)
//            }

            // Process the current movement sample in the batch (position -1)
            gamePad.processJoystickInput(event, -1)
            true
        } else {
            super.onGenericMotionEvent(event)
        }
    }
}