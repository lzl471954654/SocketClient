package SocketBeControlled

import Utils.LogUtils

fun main(args: Array<String>) {
    val client = ControlledClient("127.0.0.1",10086,"Test")
    LogUtils.initLog()
    try{
        client.clientRun()
    }catch (e:Exception){
        LogUtils.logException("Main",""+e.message)
    }
}