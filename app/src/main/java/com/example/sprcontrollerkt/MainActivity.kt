package com.example.sprcontrollerkt

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sprcontrollerkt.joystick.RockerView
import com.example.sprcontrollerkt.udp.UdpClient
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private val handler = Handler()
    private val client: UdpClient = UdpClient()
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
        val joy_x = intArrayOf(0)
        val joy_y = intArrayOf(0)
        val but_A = intArrayOf(0)
        val but_B = intArrayOf(0)
        val but_X = intArrayOf(0)
        val but_Y = intArrayOf(0)
        val but_L = intArrayOf(0)
        val but_R = intArrayOf(0)

        //开启udp消息发送线程
        Thread(object : Runnable {
            // 匿名类的Runnable接口
            @SuppressLint("SetTextI18n")
            override fun run() {
                udpSendMsg(joy_x[0].toString() + ":" + joy_y[0] + ":" + but_A[0] + ":" + but_B[0] + ":" + but_X[0] + ":" + but_Y[0] + ":" + but_L[0] + ":" + but_R[0])
                textView.text = """
                     ${joy_x[0]}
                     ${joy_y[0]}
                     ${but_A[0]}
                     ${but_B[0]}
                     ${but_X[0]}
                     ${but_Y[0]}
                     ${but_L[0]}
                     ${but_R[0]}
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
                joy_x[0] = (x / regionRadius * 128 + 128).toInt()
                joy_y[0] = (y / regionRadius * 128 + 128).toInt()
            }

            override fun onFinish() {
                joy_x[0] = 0
                joy_y[0] = 0
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
                but_A[0] = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_A[0] = 0
            }
            false
        }
        button_B.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_B[0] = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_B[0] = 0
            }
            false
        }
        button_X.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_X[0] = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_X[0] = 0
            }
            false
        }
        button_Y.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_Y[0] = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_Y[0] = 0
            }
            false
        }
        button_L.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_L[0] = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_L[0] = 0
            }
            false
        }
        button_R.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                but_R[0] = 1
            } else if (event.action == MotionEvent.ACTION_UP) {
                but_R[0] = 0
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