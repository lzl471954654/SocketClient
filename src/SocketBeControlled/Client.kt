package SocketBeControlled

import Protocol.ServerProtocol
import Utils.LogUtils
import java.io.*
import java.net.Socket
import java.net.SocketTimeoutException

class Client(val ipAddress:String,val port:Int,val myId:String,val connectedId:String){
    lateinit var reader:BufferedReader
    lateinit var writer:PrintWriter
    lateinit var socket:Socket
    val END = ServerProtocol.END_FLAG
    val builder = StringBuilder()
    var line:String? = null
    fun runClient(){
        try {
            socket = Socket(ipAddress,port)
            writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()))
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            writer.println(ServerProtocol.ONLINE+"_"+myId+"_${ServerProtocol.CONTROL}_"+END)
            writer.flush()
            println("flush")
            val onlineResult = readStringData()
            println(onlineResult)
            LogUtils.logInfo(javaClass.name,onlineResult)
            if(onlineResult.startsWith(ServerProtocol.NORMAL_MSG)&&onlineResult.endsWith(ServerProtocol.ONLINE_FAILED))
            {
                return
            }
            writer.println("${ServerProtocol.CONNECTED_TO_USER}_${ServerProtocol.BE_CONTROLLED}_$connectedId"+"_"+END)
            writer.flush()
            val connectedToUserResult = readStringData()
            println(connectedToUserResult)
            LogUtils.logInfo(javaClass.name,connectedToUserResult)
            val params = connectedToUserResult.split("_")
            when(params[0])
            {
                ServerProtocol.MAKE_HOLE->{

                }
                ServerProtocol.NORMAL_MSG->{

                }
            }
            return
        }catch (e:(IOException)){
            e.printStackTrace()
            LogUtils.logException(javaClass.name,""+e.message)
        }catch (e:SocketTimeoutException)
        {
            e.printStackTrace()
            LogUtils.logException(javaClass.name,""+e.message)
        }finally {
            reader.close()
            writer.close()
            socket.close()
            println("socket is closed")
            LogUtils.releaseResource()
        }
    }

    fun readStringData():String{
        line = null
        builder.delete(0,builder.length)
        while (true){
            line = reader.readLine()
            if((line!=null&&line!!.endsWith(END))||line==null)
                break
            builder.append(line)
        }
        return builder.toString()
    }
}