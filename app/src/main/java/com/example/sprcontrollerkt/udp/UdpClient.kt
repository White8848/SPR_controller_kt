package com.example.sprcontrollerkt.udp

import android.os.Handler
import android.os.Looper
import com.example.sprcontrollerkt.udp.UdpClient.onMsgReturnedListener
import java.lang.Exception
import java.net.*

class UdpClient {
    /**
     * 指定Server的 ip 和 port
     */
    private var mServerIp = "192.168.1.1"
    private val mServerPort = 3000
    private var mServerAddress: InetAddress? = null
    private val mUIHandler = Handler(Looper.getMainLooper())

    /**
     * 通信用的Socket
     */
    private var mSocket: DatagramSocket? = null

    //构造方法中初始化
    init {
        try {

            /*
                直接实例化一个默认的Socket对象即可，
                因为我们不需要像服务端那样把别的Client接入过来，
                不必特别明确指定 自己的ip和port（服务程序），！！！！！！！！！！
                因为这里是Client，是数据请求获取方，不是数据提供方，！！！！

                所以只需要一个默认的Socket对象
                来进行send 和 receive 即可
             */
            mSocket = DatagramSocket()
            mServerAddress = InetAddress.getByName(mServerIp)
        } catch (e: SocketException) {
            e.printStackTrace()
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }
    }

    fun setServerAddress(mServerIp: String) {
        this.mServerIp = mServerIp
        try {
            mServerAddress = InetAddress.getByName(mServerIp)
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }
    }

    fun sendMsg(msg: String, listener: onMsgReturnedListener) {
        object : Thread() {
            override fun run() {
                try {
                    //信息转型
                    val clientMsgBytes = msg.toByteArray()
                    /*
                       封装数据包，传入数据数组以及服务端地址、端口号
                    */
                    val clientPacket = DatagramPacket(
                        clientMsgBytes,
                        clientMsgBytes.size, mServerAddress, mServerPort
                    )
                    mSocket!!.send(clientPacket)

                    /*
                        接收服务端数据
                     */
                    val buf = ByteArray(1024)
                    val serverMsgPacket = DatagramPacket(buf, buf.size)
                    mSocket!!.receive(serverMsgPacket)

                    //拿到服务端地址、端口号、发送过来的数据
                    val address = serverMsgPacket.address
                    val port = serverMsgPacket.port
                    val data = serverMsgPacket.data
                    val serverMsg = String(data, 0, data.size) //把接收到的字节数据转换成String

                    /*
                        以上是信息的发送和接收，写在sendMsg 方法体中，名副其实
                        以下是对接收数据的处理，通过回调处理
                     */
                    //这里是子线程，
                    // 但是 Handler 已同 MainLooper 进行绑定，
                    // 则利用这个handle 去更新UI，等同于切回主线程更新UI
                    mUIHandler.post { //数据借助回调外传
                        //“切回了”主线程，在调用的时候，接收数据之后才能更新UI
                        listener.onMsgReturned(serverMsg)
                    }
                } catch (e: Exception) {
                    mUIHandler.post { //异常回调
                        listener.onError(e)
                    }
                }
            }
        }.start()
    }

    fun onDestroy() {
        mSocket?.close()
    }

    interface onMsgReturnedListener {
        fun onMsgReturned(msg: String?)

        /*
            Handle Exception
            如果是异步的方法调用：可以把Exception 通过 Listener 给回调出去
            如果是同步的方法调用：尽可能不要在方法中进行try catch，
            最好是将其throw 出去，
            或者catch 之后 封装下错误类型再将其throw 出去，
            即一定要让调用者能知道这个异常；

            这里是异步调用
         */
        fun onError(ex: Exception?)
    }
}