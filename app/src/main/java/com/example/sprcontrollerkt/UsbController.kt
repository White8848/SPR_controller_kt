package com.example.sprcontrollerkt

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sprcontrollerkt.gamepad.GamePad
import com.example.sprcontrollerkt.udp.UdpClient

@Suppress("DEPRECATION")
class UsbController : AppCompatActivity() {
    private val handler = Handler()
    private val client: UdpClient = UdpClient()
    private var stopUdpTask = false
    private var gamePad = GamePad()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or//全屏
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or//延申内容至状态栏
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or//隐藏导航栏
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY//沉浸式

        setContentView(R.layout.activity_usb_controller)

        val filter = IntentFilter()
        filter.addAction(TAGIN)
        filter.addAction(TAGOUT)
        registerReceiver(usbReceiver, filter)

        initViews()

        stopUdpTask = false
        handler.post(UdpTask())
    }

    fun changeController(view: View) {
        stopUdpTask = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        val buttonSetIP = findViewById<Button>(R.id.set_ip)
        val targetIP = findViewById<EditText>(R.id.target_ip)

        buttonSetIP.setOnClickListener{
                client.setServerAddress(targetIP.text.toString())
        }

    }

    private val TAGIN = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
    private val TAGOUT = "android.hardware.usb.action.USB_DEVICE_DETACHED"

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(TAGIN)) {
                Toast.makeText(context, "外设已经连接", Toast.LENGTH_SHORT).show()
            }
            if (intent.action.equals(TAGOUT)) {
                Toast.makeText(context, "外设已经移除", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class UdpTask : Runnable {
        override fun run() {
            udpSendMsg("${gamePad.getButton()}:${gamePad.getJoystick()}")

            if (!stopUdpTask) {
                handler.postDelayed(this, 50)
            }
        }
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

    private fun udpSendMsg(msg: String) {
        client.sendMsg(msg, object : UdpClient.onMsgReturnedListener {
            override fun onMsgReturned(msg: String?) {
                Log.i("UDP", msg!!)
            }


            override fun onError(ex: Exception?) {
                ex?.printStackTrace()
            }
        })
    }
}