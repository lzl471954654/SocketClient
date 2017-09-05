package SocketController;


import Utils.LogUtils;

public class MainRun {
    public static void main(String[] args) {
        try {
            LogUtils.initLog();
            LogUtils.logInfo("Main","LogInit!");
            System.out.println("123456");
            //Client client = new Client("139.199.20.248",10086,"lzl471954654","Test");
            Client client = new Client("127.0.0.1",10086,"lzl471954654","Test");
            client.runClient();
        }catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.logException("MainRun:Main",""+e.getMessage());
        }
    }
}
