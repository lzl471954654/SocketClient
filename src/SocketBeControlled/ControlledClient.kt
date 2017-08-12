package SocketBeControlled

import Protocol.ServerProtocol
import Utils.LogUtils
import java.io.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException

class ControlledClient(val ipAddress:String,val port:Int,val myId:String) {
    val reader: BufferedReader by lazy {
        BufferedReader(InputStreamReader(socket.getInputStream()))
    }

    val writer: PrintWriter by lazy {
        PrintWriter(OutputStreamWriter(socket.getOutputStream()))
    }
    lateinit var socket: Socket
    val classTag = javaClass.name
    val END = ServerProtocol.END_FLAG
    var line:String? = null
    var builder:StringBuilder = StringBuilder()
    var loop:Boolean = true
    var localPort:Int = 0

    fun clientRun(){
        try{
            socket = Socket(ipAddress,port)
            writer.println("${ServerProtocol.ONLINE}_${myId}_${ServerProtocol.BE_CONTROLLED}_$END")
            writer.flush()
            val onlineResult = readStringData()
            logInfo(onlineResult)
            while (loop){

                var instructions = readStringData()
                dealInstructions(instructions)
            }
        }catch (e:IOException){
            e.printStackTrace()
            LogUtils.logException(classTag,""+e.message)
        }catch (e:SocketException){
            LogUtils.logException(classTag,""+e.message)
        }
        finally {
            reader.close()
            writer.close()
            socket.close()
        }
    }

    fun dealInstructions(instructions:String){
        val params = instructions.split("_")
        when(params[0]){
            ServerProtocol.HEATR_BEAT->{
                println("isAlive")
            }
            ServerProtocol.MAKE_HOLE->{
                localPort = socket.localPort
                val ip = params[1]
                val port = params[2]
                logInfo("HOLE SIGN ip = $ip , port = $port")
                connectionByHole(ip,port = port.toInt())
            }
            else->{
                logInfo(instructions)
                println(instructions)
            }
        }
    }

    fun connectionByHole(ip:String,port:Int){
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
                p.println("hello ZMT ${System.currentTimeMillis()}")
                p.flush()

                var msg:String = b.readLine()
                println(msg)
                logInfo(msg)
                Thread.sleep(2000)
            }
        }.start()
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
            Thread.sleep(100)
        }
        return builder.toString()
    }

    fun logInfo(msg:String){
        println(msg)
        LogUtils.logInfo(classTag,msg)
    }
}