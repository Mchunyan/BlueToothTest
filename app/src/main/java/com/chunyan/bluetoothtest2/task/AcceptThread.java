package com.chunyan.bluetoothtest2.task;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.chunyan.bluetoothtest2.callback.ServiceCallback;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class AcceptThread extends Thread {
    private static final String TAG = "mcy";
    private ServiceCallback callBack;
    private BluetoothServerSocket serverSocket = null;
    private InputStream inputStream;
    private BufferedInputStream bufferedInputStream;
    private byte[] data;

    public AcceptThread(String name, String uuid, BluetoothAdapter bluetoothAdapter, ServiceCallback callBack) {
        this.callBack = callBack;
        try {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, UUID.fromString(uuid));

        } catch (IOException e) {
            e.printStackTrace();
            callBack.onConnectFail(e.getMessage());
        }
    }

    /**
     * 关闭各种流和通信的socket
     */
    public void closeSocket() {
        try {
            inputStream.close();
            bufferedInputStream.close();
            serverSocket.close();
            callBack.onConnectClose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            final BluetoothSocket bluetoothSocket = serverSocket.accept();
            callBack.onConnectSuccess();
            inputStream = bluetoothSocket.getInputStream();//获取服务端发来的消息
            bufferedInputStream = new BufferedInputStream(inputStream);
            while (true) {
                int available =0;
                while(available==0){
                    available=inputStream.available();
                }
                data = new byte[available];
                bufferedInputStream.read(data);
                callBack.onResultMessage(data);//回传数据
            }
        } catch (IOException e) {
            callBack.onConnectFail(e.getMessage());
            e.printStackTrace();
        }
    }
}
