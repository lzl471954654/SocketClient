package SocketController

import Protocol.ServerProtocol
import Utils.LogUtils
import java.io.*
import java.net.*

class Client(val ipAddress:String,val port:Int,val myId:String,val connectedId:String){
    lateinit var reader:BufferedReader
    lateinit var writer:PrintWriter
    lateinit var socket:Socket
    val END = ServerProtocol.END_FLAG
    val builder = StringBuilder()
    var line:String? = null
    var localPort = 0
    fun runClient(){
        try {
            socket = Socket()
            socket.reuseAddress = true
            socket.connect(InetSocketAddress(ipAddress,port))
            writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()))
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            writer.println(ServerProtocol.ONLINE+"_"+myId+"_${ServerProtocol.CONTROL}_"+END)
            writer.flush()
            val onlineResult = readStringData()
            logInfo(onlineResult)
            if(onlineResult.startsWith(ServerProtocol.NORMAL_MSG)&&onlineResult.endsWith(ServerProtocol.ONLINE_FAILED))
            {
                return
            }
            writer.println("${ServerProtocol.CONNECTED_TO_USER}_${ServerProtocol.CONTROL}_$connectedId"+"_"+END)
            writer.flush()
            val connectedToUserResult = readStringData()
            logInfo(connectedToUserResult)
            val params = connectedToUserResult.split("_")

            when(params[0])
            {
                ServerProtocol.MAKE_HOLE->{
                    localPort = socket.localPort

                    connectionByHole(params[1],params[2].toInt(),false)
                }
                ServerProtocol.NORMAL_MSG->{

                }
            }
        }catch (e:(IOException)){
            e.printStackTrace()
            LogUtils.logException(javaClass.name,""+e.message)
        }catch (e:SocketTimeoutException)
        {
            e.printStackTrace()
            LogUtils.logException(javaClass.name,""+e.message)
        }finally {
            writer.println("${ServerProtocol.OFFLINE}_${myId}_${ServerProtocol.CONTROL}_${ServerProtocol.END_FLAG}")
            writer.flush()
            socket.shutdownInput()
            socket.shutdownOutput()
            socket.close()
            println("socket is closed")
            LogUtils.releaseResource()
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