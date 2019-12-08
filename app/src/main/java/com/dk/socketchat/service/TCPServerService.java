package com.dk.socketchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dk.socketchat.util.MsgUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TCPServerService extends Service {

    private static final String TAG = TCPServerService.class.getSimpleName();
    private boolean isServiceDestroy = false;
    private String serverUrl = "localhost";
    private int port = 8688;
    private ServerSocket serverSocket;
    private String[] ramdomResp = {
            "你好啊", "很高兴", "第一次见面", "第2次见面",
            "第3次见面", "第4次见面", "很高兴收到你的消息", "第6次见面", "第7次见面",
    };

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new ListenClientRunnable()).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceDestroy = true;
        //A销毁socket
    }

    private class ListenClientRunnable implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "建立server监听失败:"+e.getMessage());
                return;
            }
            while (!isServiceDestroy) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    Log.e(TAG, MsgUtil.timeFormat(System.currentTimeMillis()) + "-建立连接");
                    //开启子线程处理每一个client的请求
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handleClient(clientSocket);
                        }
                    }).start();
                } catch (IOException e) {
                    Log.e(TAG, "Server连接失败");
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        if (clientSocket == null) {
            return;
        }
        try {
            //读取
            InputStream inputStream = clientSocket.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            //回复client
            OutputStream outputStream = clientSocket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            PrintWriter printWriter = new PrintWriter(outputStreamWriter, true);
            while (!isServiceDestroy) {
                String clientMsg = bufferedReader.readLine();
                if (clientMsg == null) {
                    Log.e(TAG, "客户端断开连接");
                    break;
                }
                Log.e(TAG, "读取client发送数据：" + clientMsg);

                int index = new Random().nextInt(ramdomResp.length);
                String respStr = MsgUtil.timeFormat(System.currentTimeMillis()) + ramdomResp[index];
                printWriter.println("很高兴加入聊天");
                printWriter.println(respStr);
                Log.e(TAG, "发给client的数据：" + respStr);
            }
            //关闭流
            bufferedReader.close();
            printWriter.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "读取client数据异常");
        }
    }
}
