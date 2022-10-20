package com.example.sprcontrollerkt

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sprcontrollerkt.gamepad.GamePad
import com.example.sprcontrollerkt.joystick.RockerView
import com.example.sprcontrollerkt.udp.UdpClient


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private val handler = Handler()
    private val client: UdpClient = UdpClient()
    private var gamePad = GamePad()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //隐藏标题
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) //设置全屏
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(R.layout.activity_main)
        initViews()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent):Boolean {
        return if (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            gamePad.setKeyCode(keyCode)
            gamePad.updateKeyValue(1)
            true
        } else{
            super.onKeyUp(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return if (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            gamePad.setKeyCode(keyCode)
            gamePad.updateKeyValue(0)
            true
        } else{
            super.onKeyUp(keyCode, event)
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        // Check that the event came from a game controller
        return if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
            && event.action == MotionEvent.ACTION_MOVE) {

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

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        val textView = findViewById<TextView>(R.id.textView2)
        val rockerView: RockerView = findViewById(R.id.rockerView1)
        val button_A = findViewById<Button>(R.id.button_A)
        val button_B = findViewById<Button>(R.id.button_B)
        val button_X = findViewById<Button>(R.id.button_X)
        val button_Y = findViewById<Button>(R.id.button_Y)
        val button_L = findViewById<Button>(R.id.button_L)
        val button_R = findViewById<Button>(R.id.button_R)
        val set_ip = findViewById<Button>(R.id.set_ip)
        val target_ip = findViewById<EditText>(R.id.target_ip)

        //虚拟按键读取值
        var joy_x = 0
        var joy_y = 0
        var but_A = 0
        var but_B = 0
        var but_X = 0
        var but_Y = 0
        var but_L = 0
        var but_R = 0

        //开启udp消息发送线程
        Thread(object : Runnable {
            // 匿名类的Runnable接口
            @SuppressLint("SetTextI18n")
            override fun run() {
                udpSendMsg("$joy_x:$joy_y:$but_A:$but_B:$but_X:$but_Y:$but_L:$but_R")
                textView.text = """
                     $joy_x
                     $joy_y
                     $but_A
                     $but_B
                     $but_X
                     $but_Y
                     $but_L
                     $but_R
                     """.trimIndent()
                handler.postDelayed(this, 50)
            }
        }).start()

        // 设置回调模式
        rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_MOVE)

        // 监听摇动方向
        rockerView.setOnAngleChangeListener(object : RockerView.OnAngleChangeListener {
            override fun onStart() {}
            override fun angle(angle: Double) {
                //Log.i("UDP", "angle: " + angle);
            }

            override fun value(x: Float, y: Float, regionRadius: Float) {
                joy_x = (x / regionRadius * 128 + 128).toInt()
                joy_y = (y / regionRadius * 128 + 128).toInt()
            }

            override fun onFinish() {
                joy_x = 0
                joy_y = 0
            }
        })
        set_ip.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                client.setServerAddress(target_ip.text.toString())
            }
            false
        }
        button_A.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_A = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_A = 0
            }
            false
        }
        button_B.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_B = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_B = 0
            }
            false
        }
        button_X.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_X = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_X = 0
            }
            false
        }
        button_Y.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_Y = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_Y = 0
            }
            false
        }
        button_L.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_L = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_L = 0
            }
            false
        }
        button_R.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_R = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_R = 0
            }
            false
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

    /*
        回收资源
     */
    override fun onDestroy() {
        super.onDestroy()
        client.onDestroy()
    }
}