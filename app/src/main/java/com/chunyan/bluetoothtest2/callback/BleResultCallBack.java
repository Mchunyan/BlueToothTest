package com.chunyan.bluetoothtest2.callback;

public interface BleResultCallBack {
    void onReturnResult(byte[] data);

    void onDiscoverServicesSuccess();
}
