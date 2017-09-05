package SocketController

import Protocol.ServerProtocol
import Utils.LogUtils
import java.io.*
import java.net.*

class Client(val ipAddress:String,val port:Int,val username:String,val password:String){
    lateinit var reader:BufferedReader
    lateinit var writer:PrintWriter
    lateinit var socket:Socket
    val classTag = javaClass.name
    val END = ServerProtocol.END_FLAG
    val builder = StringBuilder()
    var line:String? = null
    var localPort = 0
    fun runClient(){
        try {
            socket = Socket(ipAddress,port)
            writer = PrintWriter(socket.getOutputStream())
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            writer.println(ServerProtocol.CONNECTED_TO_USER+"_${username}_${password}_${ServerProtocol.END_FLAG}")
            writer.flush()
            line = readStringData()
            if(line!=null){
                if(line!!.startsWith(ServerProtocol.CONNECTED_SUCCESS)&&line!!.endsWith(ServerProtocol.END_FLAG)){
                    loop()
                }
                else{
                    LogUtils.logInfo(classTag,line!!)
                }
            }
            else{
                LogUtils.logInfo(classTag,"No Message from server,Connected to user failed")
            }
        }catch (e:(IOException)){
            e.printStackTrace()
            LogUtils.logException(classTag,""+e.message)
        }catch (e:SocketTimeoutException)
        {
            e.printStackTrace()
            LogUtils.logException(classTag,""+e.message)
        }finally {
            socket.close()
            println("socket is closed")
            LogUtils.releaseResource()
        }
    }
    fun loop(){
        var sendString:String? = ""
        while (true){
            sendString = readLine()
            if(sendString==null)
            {
                println("Exit")
                break
            }
            writer.println(sendString+"_${ServerProtocol.END_FLAG}")
            writer.flush()
            sendString = readStringData()
            println("From Server: ${sendString}")
        }
    }

    fun connectionByHole(ip: String,port: Int,UDP:Boolean){
        Thread{
            try {
                var bytes:ByteArray? = null
                var outpacket: DatagramPacket? =null
                var inbyte = ByteArray(1024)
                var data = "Hello LZL! ${System.currentTimeMillis()}"
                bytes = data.toByteArray()
                outpacket = DatagramPacket(bytes,bytes!!.size,InetSocketAddress(ip,port))
                var socket: DatagramSocket = DatagramSocket(localPort)
                var inpacket = DatagramPacket(inbyte,inbyte.size)
                socket.send(outpacket)
                Thread.sleep(500)
                socket.send(outpacket)
                while (true){
                    socket.receive(inpacket)
                    logInfo(String(inpacket.data,0,inpacket.data.size))
                    socket.send(outpacket)
                    Thread.sleep(1000)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }.start()
    }

    fun connectionByHole(ip:String,port:Int){
        logInfo("HOLE SIGN ip = $ip , port = $port")
        Thread{
            val newSocket:Socket = Socket()
            newSocket.reuseAddress = true
            newSocket.bind(InetSocketAddress(InetAddress.getLocalHost().hostAddress,localPort))
            logInfo("connect to ${InetSocketAddress(ip,port)}")
            newSocket.connect(InetSocketAddress(ip,port))
            logInfo("connection success")
            val b: BufferedReader = BufferedReader(InputStreamReader(newSocket.getInputStream()))
            val p: PrintWriter = PrintWriter(OutputStreamWriter(newSocket.getOutputStream()))
            while (true){
                p.println("hello LZL ${System.currentTimeMillis()}")
                p.flush()

                var msg:String = b.readLine()
                println(msg)
                logInfo(msg)
                Thread.sleep(2000)
            }
        }.start()
    }

    fun logInfo(msg:String){
        println(msg)
        LogUtils.logInfo(javaClass.name,msg)
    }

    fun readStringData():String{
        line = null
        builder.delete(0,builder.length)
        while (true){
            line = reader.readLine()
            if(line!=null)
                builder.append(line)
            if(line!=null&&line!!.endsWith(END))
                break
            Thread.sleep(50)
        }
        return builder.toString()
    }
}