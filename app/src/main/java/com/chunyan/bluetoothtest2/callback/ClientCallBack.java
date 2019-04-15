package com.chunyan.bluetoothtest2.callback;

import android.bluetooth.BluetoothDevice;

public interface ClientCallBack {
    //开始扫描
    void onScanStarted();

    //扫描结束
    void onScanFinished();

    //扫描中
    void onScanning(BluetoothDevice device);

    //配对请求
    void onBondRequest();

    //配对成功
    void onBondSuccess(BluetoothDevice device);

    //正在配对
    void onBonding(BluetoothDevice device);

    //配对失败
    void onBondFail(BluetoothDevice device);

    //连接成功
    void onConnectSuccess();

    //连接失败
    void onConnectFail(String errorMsg);

    //连接关闭
    void onConnectClose();
}
