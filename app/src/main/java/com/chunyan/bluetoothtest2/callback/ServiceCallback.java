package com.chunyan.bluetoothtest2.callback;

/**
 * Created by machunyan on 2019/4/14.
 */
public interface ServiceCallback {
    //连接成功
    void onConnectSuccess();

    //连接失败
    void onConnectFail(String errorMsg);

    void onConnectClose();

    void onResultMessage(byte[] data);
}
