package com.dk.socketchat.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dk.socketchat.R;
import com.dk.socketchat.service.TCPServerService;
import com.dk.socketchat.util.MsgUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = ChatFragment.class.getSimpleName();
    public static final String DEFAULT_USER = "DefaultUser";
    public static final String CHAT_ARGMENTS = "chat_argments";
    private String userName;
    private Activity mContext;
    private EditText sendInput;
    private Button btnSend;
    private TextView chatContanier;
    private static final int MSG_CONNECTED = 200;
    private static final int MSG_DETAIL = 1000;
    private Socket mSocket;
    private PrintWriter mPrintWiter;
    private int tryTimes = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (Activity) context;
        userName = getArguments() != null ? getArguments().getString(CHAT_ARGMENTS) : DEFAULT_USER;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent service = new Intent(mContext, TCPServerService.class);
        mContext.startService(service);
        //tcp连接耗时，发送接收耗时，开启子线程连接和接受msg
        new Thread(){
            @Override
            public void run() {
                connectToServer();
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        if (mSocket != null) {
            try {
                mSocket.shutdownInput();
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    private void connectToServer() {
        //连接server
        Socket socket = null;
        while (socket == null && tryTimes < 10) {//未连接成功，循环连接
            try {
                socket = new Socket("localhost", 8688);
                mSocket = socket;
                OutputStream outputStream = socket.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                mPrintWiter = new PrintWriter(bufferedWriter, true);
                MsgHanler.sendEmptyMessage(MSG_CONNECTED);
                Log.e(TAG, "连接Server成功");
            } catch (IOException e) {
                Log.e(TAG, "连接server异常：" + e.getMessage());
                SystemClock.sleep(1000);//初始连接失败后，1s后重连
                tryTimes++;
            }
        }
        if (socket == null) {
            Log.e(TAG, "尝试次数过多，连接失败");
            return;
        }

        //接受消息
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while (!mContext.isFinishing()) {
                String recvMsg = bufferedReader.readLine();
                MsgHanler.sendMessage(MsgHanler.obtainMessage(MSG_DETAIL, recvMsg));
            }
            Log.e(TAG, "退出。。。");
            socket.shutdownInput();
            inputStreamReader.close();
            bufferedReader.close();
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "读取server信息异常：" + e.getMessage());
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        sendInput = (EditText) view.findViewById(R.id.send_input);
        btnSend = (Button) view.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);
        chatContanier = (TextView) view.findViewById(R.id.chat_container);
        if (!userName.equals(DEFAULT_USER)) {
            btnSend.setEnabled(true);
        }
        return view;
    }

    //todo发送msg

    //hanler
    private Handler MsgHanler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_CONNECTED:
                    enableSendBtn();
                    break;
                case MSG_DETAIL:
                    showRespMsg(msg.obj);
                    break;
            }
        }
    };

    private void showRespMsg(Object obj) {
        String msg = (String) obj;
        if (chatContanier != null) {
            String newMsg = chatContanier.getText().toString() + "\n" + msg;
            chatContanier.setText(newMsg);
        }
    }

    private void enableSendBtn() {
        if (btnSend != null) {
            btnSend.setEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                sendMsg();
                break;
        }
    }

    private void sendMsg() {
        //发送消息,
        final String msgToSend = sendInput.getText().toString();
        if (!msgToSend.isEmpty() && mSocket != null && mPrintWiter != null) {
            new Thread(){
                @Override
                public void run() {
                    try {
                        mPrintWiter.println(msgToSend);//通过outPutStream发出
                    } catch (Exception e) {
                        Log.e(TAG, "发送消息失败：" + e.getMessage());
                        return;
                    }
                }
            }.start();
            sendInput.setText("");//清空输入框
            //更新聊天记录
            String selfMsg = MsgUtil.timeFormat(System.currentTimeMillis()) + "- Me:" + msgToSend;
            String newMsg = chatContanier.getText().toString() + "\n" + selfMsg;
            chatContanier.setText(newMsg);
        }
    }
}
