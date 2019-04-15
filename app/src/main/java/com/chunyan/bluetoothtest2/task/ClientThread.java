package com.chunyan.bluetoothtest2.task;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.chunyan.bluetoothtest2.callback.ClientCallBack;

import java.io.IOException;
import java.io.OutputStream;

public class ClientThread extends Thread {

    private static final String TAG = "mcy";
    private ClientCallBack callBack;
    private BluetoothSocket bluetoothSocket = null;
    private Handler handler;
    private OutputStream outputStream;

    public ClientThread(BluetoothSocket bluetoothSocket, ClientCallBack callBack) {
        this.callBack = callBack;
        this.bluetoothSocket = bluetoothSocket;
    }

    /**
     * 写数据
     *
     * @param data
     */

    public void write(byte[] data) {
        Message message = new Message();
        message.obj = data;
        handler.sendMessage(message);
    }

    /**
     * 关闭各种连接连接
     */
    public void closeSocket() {
        try {
            outputStream.close();
            bluetoothSocket.close();
            callBack.onConnectClose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();//读取需要发送的的数据
            Looper.prepare();
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    try {
                        byte[] data = (byte[]) msg.obj;
                        outputStream.write(data);
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            callBack.onConnectSuccess();
            Looper.loop();
        } catch (IOException e) {
            callBack.onConnectFail(e.getMessage());
        }
    }
}
