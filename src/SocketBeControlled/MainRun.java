package SocketBeControlled;


import Utils.LogUtils;

public class MainRun {
    public static void main(String[] args) {
        try {
            LogUtils.initLog();
            LogUtils.logInfo("Main","LogInit!");
            System.out.println("123456");
            Client client = new Client("192.168.1.121",10086,"lzl471954654","Test");
            client.runClient();
        }catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.logException("MainRun:Main",""+e.getMessage());
        }
    }
}
