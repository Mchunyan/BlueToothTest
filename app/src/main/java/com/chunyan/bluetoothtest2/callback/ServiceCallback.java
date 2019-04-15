package com.chunyan.bluetoothtest2.callback;

/**
 * Created by machunyan on 2019/4/14.
 */
public interface ServiceCallback {
    //连接成功
    void onConnectSuccess();

    //连接失败
    void onConnectFail(String errorMsg);

    //连接关闭
    void onConnectClose();

    //接收到的数据
    void onResultMessage(byte[] data);
}
