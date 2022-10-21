package com.example.sprcontrollerkt

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sprcontrollerkt.joystick.RockerView
import com.example.sprcontrollerkt.udp.UdpClient


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private val handler = Handler()
    private val client: UdpClient = UdpClient()
    private var stopUdpTask = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or//全屏
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or//延申内容至状态栏
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or//隐藏导航栏
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY//沉浸式

        //生成界面
        setContentView(R.layout.activity_main)
        //初始化界面元素
        initViews()
        //开启udp发送线程
        stopUdpTask = false
        handler.post(UdpTask())
    }

    fun changeController(view: View) {
        stopUdpTask = true
        val intent = Intent(this, UsbController::class.java)
        startActivity(intent)
    }

    //虚拟按键读取值
    var joyX = 128
    var joyY = 128
    var butA = 0
    var butB = 0
    var butX = 0
    var butY = 0
    var butL = 0
    var butR = 0

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        //界面元素初始化
        val rockerView: RockerView = findViewById(R.id.rockerView1)
        val buttonA = findViewById<Button>(R.id.button_A)
        val buttonB = findViewById<Button>(R.id.button_B)
        val buttonX = findViewById<Button>(R.id.button_X)
        val buttonY = findViewById<Button>(R.id.button_Y)
        val buttonL = findViewById<Button>(R.id.button_L)
        val buttonR = findViewById<Button>(R.id.button_R)
        val buttonSetIP = findViewById<Button>(R.id.set_ip)
        val targetIP = findViewById<EditText>(R.id.target_ip)

        // 设置虚拟摇杆回调模式
        rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_MOVE)

        // 监听摇杆移动方向
        rockerView.setOnAngleChangeListener(object : RockerView.OnAngleChangeListener {
            override fun onStart() {}
            override fun angle(angle: Double) {
                //Log.i("UDP", "angle: " + angle);
            }

            override fun value(x: Float, y: Float, regionRadius: Float) {
                joyX = (x / regionRadius * 128 + 128).toInt()
                joyY = (y / regionRadius * 128 + 128).toInt()
            }

            override fun onFinish() {
                joyX = 128
                joyY = 128
            }
        })
        buttonSetIP.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                client.setServerAddress(targetIP.text.toString())
            }
            false
        }
        buttonA.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                butA = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                butA = 0
            }
            false
        }
        buttonB.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                butB = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                butB = 0
            }
            false
        }
        buttonX.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                butX = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                butX = 0
            }
            false
        }
        buttonY.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                butY = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                butY = 0
            }
            false
        }
        buttonL.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                butL = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                butL = 0
            }
            false
        }
        buttonR.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                butR = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                butR = 0
            }
            false
        }

    }

    private inner class UdpTask : Runnable {
        val textView: TextView = findViewById(R.id.textView2)
        override fun run() {
            udpSendMsg("$joyX:$joyY:$butA:$butB:$butX:$butY:$butL:$butR")
            textView.text = """
                     ${joyX - 128}
                     ${joyY - 128}
                     $butA
                     $butB
                     $butX
                     $butY
                     $butL
                     $butR
                     """.trimIndent()
            if (!stopUdpTask) {
                handler.postDelayed(this, 50)
            }
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